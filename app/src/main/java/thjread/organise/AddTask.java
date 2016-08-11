package thjread.organise;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddTask extends DialogFragment {
    private ArrayList<String> locations;

    class FinalBoolean {
        public boolean value;

        FinalBoolean(boolean val) {
            value = val;
        }
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        final EditText titleText = (EditText) view.findViewById(R.id.edit_task_name);

        final FinalBoolean deadlineDateSet = new FinalBoolean(false);
        final Calendar deadlineDate = Calendar.getInstance();
        Button deadlineDatePick = (Button) view.findViewById(R.id.edit_task_deadline);
        dateSetter(view, deadlineDateSet, deadlineDate, deadlineDatePick);

        final FinalBoolean scheduleDateSet = new FinalBoolean(false);
        final Calendar scheduleDate = Calendar.getInstance();
        Button scheduleDatePick = (Button) view.findViewById(R.id.edit_task_schedule);
        dateSetter(view, scheduleDateSet, scheduleDate, scheduleDatePick);

        builder.setMessage("Add task")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String pathString = (String) spinner.getSelectedItem();
                        List<String> path = Arrays.asList(pathString.split("/"));
                        OrgItem parent = null;
                        Org doc;
                        if (path.size() > 1) {
                            parent = GlobalState.getFiles().getItem(path);
                        }
                        if (parent != null) {
                            doc = parent.document;
                        } else {
                            doc = GlobalState.getFiles().getDocument(path.get(0));
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

                        doc.addItem(parent, orgItem);
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
