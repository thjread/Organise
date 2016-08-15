package thjread.organise;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.app.DatePickerDialog;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddTask extends AppCompatActivity {
    class FinalBoolean {
        public boolean value;

        FinalBoolean(boolean val) {
            value = val;
        }
    }

    private static final String ARG_PATH = "path";
    private static final String ARG_DOC = "document";
    private static final String ARG_CHILD_NUMBER = "child_number";
    private static final String ARG_IS_EDIT = "is_edit";
    private static final String ARG_SHOW_SPINNER = "show_spinner";
    private OrgItem item = null;
    private Org document = null;
    private Integer childNumber = null;
    private boolean isEdit = false;
    private boolean showSpinner = true;
    private OrgItem changedItem = null;

    public static final String RESULT_ITEM_PATH = "ITEM_PATH";
    public static final String RESULT_IS_EDIT = "IS_EDIT";
    public static final String RESULT_IS_DELETE = "IS_DELETE";

    private CardView cardView;

    public static Intent newInstance(Activity fromActivity, OrgItem item, Org document, Integer childNumber, Boolean isEdit, Boolean showSpinner) {
        Bundle args = new Bundle();
        if (item != null) {
            ArrayList<String> path = item.getPath();
            String pathString = "";
            for (int i = 0; i < path.size(); ++i) {
                pathString += path.get(i);
                if (i != path.size() - 1) {
                    pathString += "\n";
                }
            }
            args.putString(ARG_PATH, pathString);
        }
        if (document != null) {
            args.putString(ARG_DOC, document.title);
        }
        if (childNumber != null) {
            args.putInt(ARG_CHILD_NUMBER, childNumber);
        }
        if (isEdit != null) {
            args.putBoolean(ARG_IS_EDIT, isEdit);
        }
        if (showSpinner != null) {
            args.putBoolean(ARG_SHOW_SPINNER, showSpinner);
        }
        Intent i = new Intent(fromActivity, AddTask.class);
        i.putExtras(args);
        return i;
    }

    private boolean runAnimation = false;

    public void onResume() {
        super.onResume();

        if (runAnimation) {
            if (Build.VERSION.SDK_INT >= 21) {
                ValueAnimator anim = new ValueAnimator();
                anim.setIntValues(getResources().getColor(R.color.colorAccent),
                        getResources().getColor(R.color.card_background));
                anim.setEvaluator(new ArgbEvaluator());
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (Integer) animation.getAnimatedValue();
                        cardView.setBackgroundColor(color);
                    }
                });
                anim.setDuration(125);
                anim.start();
                runAnimation = false;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            ChangeBounds bounds = new ChangeBounds();
            bounds.setDuration(125);
            getWindow().setSharedElementEnterTransition(bounds);
            runAnimation = true;
        }

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String path_string = args.getString(ARG_PATH);
            if (path_string != null) {
                List<String> path = Arrays.asList(path_string.split("\n"));
                item = GlobalState.getFiles().getItem(path);
            }
            String doc_title = args.getString(ARG_DOC);
            if (doc_title != null) {
                document = GlobalState.getFiles().getDocument(doc_title);
            }
            childNumber = args.getInt(ARG_CHILD_NUMBER, -1);
            if (childNumber == -1) {
                childNumber = null;
            }
            isEdit = args.getBoolean(ARG_IS_EDIT, false);
            showSpinner = args.getBoolean(ARG_SHOW_SPINNER, true);
        }

        setContentView(R.layout.activity_add_task);
        //view.setPadding(19, 5, 14, 5); //Magic padding values

        cardView = (CardView) findViewById(R.id.add_task_card_view);

        List<String> locations = GlobalState.getLocations();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        if (!showSpinner) {
            spinner.setVisibility(View.GONE);
        }

        final EditText titleText = (EditText) findViewById(R.id.edit_task_name);

        final FinalBoolean deadlineDateSet = new FinalBoolean(false);
        final Calendar deadlineDate = Calendar.getInstance();
        Button deadlineDatePick = (Button) findViewById(R.id.edit_task_deadline);

        final FinalBoolean scheduleDateSet = new FinalBoolean(false);
        final Calendar scheduleDate = Calendar.getInstance();
        Button scheduleDatePick = (Button) findViewById(R.id.edit_task_schedule);

        if (isEdit) {
            titleText.setText(item.title);
            if (item.deadline != null) {
                deadlineDate.setTime(item.deadline);
                deadlineDateSet.value = true;
                deadlineDatePick.setText(DateFormatter.format(item.deadline, false));
            }
            if (item.scheduled != null) {
                scheduleDate.setTime(item.scheduled);
                scheduleDateSet.value = true;
                scheduleDatePick.setText(DateFormatter.format(item.scheduled, false));
            }
        }

        dateSetter(deadlineDateSet, deadlineDate, deadlineDatePick);
        dateSetter(scheduleDateSet, scheduleDate, scheduleDatePick);

        findViewById(R.id.add_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    item.title = titleText.getText().toString();
                    if (deadlineDateSet.value) {
                        item.deadline = deadlineDate.getTime();
                    } else {
                        item.deadline = null;
                    }
                    if (scheduleDateSet.value) {
                        item.scheduled = scheduleDate.getTime();
                    } else {
                        item.scheduled = null;
                    }
                    item.document.file.write(item.document);
                    changedItem = item;
                } else {
                    OrgItem parent = null;
                    Org doc;
                    if (!showSpinner) {
                        parent = item;
                        doc = document;
                    } else {
                        String pathString = (String) spinner.getSelectedItem();
                        List<String> path = Arrays.asList(pathString.split("\n"));
                        if (path.size() > 1) {
                            parent = GlobalState.getFiles().getItem(path);
                        }
                        if (parent != null) {
                            doc = parent.document;
                        } else {
                            doc = GlobalState.getFiles().getDocument(path.get(0));
                        }
                    }
                    changedItem = new OrgItem(doc.keyword, null, parent, 0, doc);
                    changedItem.title = titleText.getText().toString();
                    if (deadlineDateSet.value) {
                        changedItem.deadline = deadlineDate.getTime();
                    }
                    if (scheduleDateSet.value) {
                        changedItem.scheduled = scheduleDate.getTime();
                    }
                    if (parent != null) {
                        changedItem.treeLevel = parent.treeLevel + 1;
                    } else {
                        changedItem.treeLevel = 1;
                    }
                    changedItem.keyword = changedItem.keywords
                            .keywordToInt(changedItem.keywords.todoKeywords.get(0));

                    doc.addItem(parent, changedItem, childNumber);
                }

                Intent data = new Intent();

                data.putExtra(RESULT_ITEM_PATH, changedItem.getPath());
                data.putExtra(RESULT_IS_EDIT, isEdit);
                data.putExtra(RESULT_IS_DELETE, false);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        findViewById(R.id.add_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.add_task_container).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                finish();
                return true;
            }
        });

        //dialog.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
    }

    public void dateSetter(final FinalBoolean dateSet, final Calendar date, final Button datePick) {
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                datePick.setText(DateFormatter.format(date.getTime(), false));
                dateSet.value = true;
            }
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));

        datePick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (dateSet.value) {
                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Remove", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                dateSet.value = false;
                                datePick.setText(getResources().getString(R.string.none));
                            }
                        }
                    });
                } else {
                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                }
                datePickerDialog.show();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
