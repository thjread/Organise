package thjread.organise;

import android.support.v7.app.AlertDialog;
import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class AddTask extends AppCompatDialogFragment {
    private ArrayList<String> locations;

    class FinalBoolean {
        public boolean value;

        FinalBoolean(boolean val) {
            value = val;
        }
    }

    private static final String ARG_PATH = "path";
    private static final String ARG_CHILD_NUMBER = "child_number";
    private static final String ARG_IS_EDIT = "is_edit";
    private OrgItem item;
    private Integer childNumber;
    private boolean isEdit;

    public static AddTask newInstance(OrgItem item, Integer childNumber, Boolean isEdit) {
        AddTask fragment = new AddTask();
        Bundle args = new Bundle();
        if (item != null) {
            ArrayList<String> path = item.getPath();
            String pathString = "";
            for (int i = 0; i < path.size(); ++i) {
                pathString += path.get(i);
                if (i != path.size() - 1) {
                    pathString += "/";
                }
            }
            args.putString(ARG_PATH, pathString);
        }
        if (childNumber != null) {
            args.putInt(ARG_CHILD_NUMBER, childNumber);
        }
        if (isEdit != null) {
            args.putBoolean(ARG_IS_EDIT, isEdit);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String path_string = getArguments().getString(ARG_PATH);
            if (path_string != null) {
                List<String> path = Arrays.asList(path_string.split("/"));
                item = GlobalState.getFiles().getItem(path);
            }
            childNumber = getArguments().getInt(ARG_CHILD_NUMBER, -1);
            if (childNumber == -1) {
                childNumber = null;
            }
            isEdit = getArguments().getBoolean(ARG_IS_EDIT, false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_task, null, false);
        view.setPadding(19, 5, 14, 5); //Magic padding values
        builder.setView(view);

        locations = new ArrayList<>();
        locations.add("Todo.org/Tasks");
        locations.add("Todo.org/Problems");
        locations.add("Homework.org");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, locations);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        if (item != null) {
            spinner.setVisibility(View.GONE);
        }

        final EditText titleText = (EditText) view.findViewById(R.id.edit_task_name);

        final FinalBoolean deadlineDateSet = new FinalBoolean(false);
        final Calendar deadlineDate = Calendar.getInstance();
        Button deadlineDatePick = (Button) view.findViewById(R.id.edit_task_deadline);

        final FinalBoolean scheduleDateSet = new FinalBoolean(false);
        final Calendar scheduleDate = Calendar.getInstance();
        Button scheduleDatePick = (Button) view.findViewById(R.id.edit_task_schedule);

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

        dateSetter(view, deadlineDateSet, deadlineDate, deadlineDatePick);
        dateSetter(view, scheduleDateSet, scheduleDate, scheduleDatePick);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isEdit) {
                            item.title = titleText.getText().toString();
                            if (deadlineDateSet.value == true) {
                                item.deadline = deadlineDate.getTime();
                            } else {
                                item.deadline = null;
                            }
                            if (scheduleDateSet.value == true) {
                                item.scheduled = scheduleDate.getTime();
                            } else {
                                item.scheduled = null;
                            }
                            item.document.file.write(item.document);
                        } else {
                            OrgItem parent = null;
                            Org doc;
                            if (item != null) {
                                parent = item;
                                doc = item.document;
                            } else {
                                String pathString = (String) spinner.getSelectedItem();
                                List<String> path = Arrays.asList(pathString.split("/"));
                                if (path.size() > 1) {
                                    parent = GlobalState.getFiles().getItem(path);
                                }
                                if (parent != null) {
                                    doc = parent.document;
                                } else {
                                    doc = GlobalState.getFiles().getDocument(path.get(0));
                                }
                            }
                            OrgItem orgItem = new OrgItem(doc.keyword, null, parent, 0, doc);
                            orgItem.title = titleText.getText().toString();
                            if (deadlineDateSet.value == true) {
                                orgItem.deadline = deadlineDate.getTime();
                            }
                            if (scheduleDateSet.value == true) {
                                orgItem.scheduled = scheduleDate.getTime();
                            }
                            if (parent != null) {
                                orgItem.treeLevel = parent.treeLevel + 1;
                            } else {
                                orgItem.treeLevel = 1;
                            }
                            orgItem.keyword = orgItem.keywords
                                    .keywordToInt(orgItem.keywords.todoKeywords.get(0));

                            doc.addItem(parent, orgItem, childNumber);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void dateSetter(View view, final FinalBoolean dateSet, final Calendar date, final Button datePick) {
        final DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                datePick.setText(DateFormatter.format(date.getTime(), false));
                dateSet.value = true;
            }
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));

        datePick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (dateSet.value == true) {
                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Remove", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                dateSet.value = false;
                                datePick.setText("None");
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
    public void onDetach() {
        super.onDetach();
    }
}
