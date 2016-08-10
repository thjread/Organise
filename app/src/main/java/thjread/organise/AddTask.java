package thjread.organise;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddTask extends DialogFragment {
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat simpleDateFormat;

    private ArrayList<ArrayList<String>> locations;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_task, null, false);
        view.setPadding(19, 5, 14, 5); //Magic padding values
        builder.setView(view);

        final EditText titleText = (EditText) view.findViewById(R.id.edit_task_name);

        final EditText datePick = (EditText) view.findViewById(R.id.edit_task_date);
        datePick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                datePickerDialog.show();
                return true;
            }
        });

        Calendar c = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                datePick.setText(simpleDateFormat.format(newDate.getTime()));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        builder.setMessage("Add task")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Org doc = GlobalState.getFiles().getFiles().get(0);
                        OrgItem orgItem = new OrgItem(doc.keyword, null, null, 0, doc);
                        orgItem.title = titleText.getText().toString();
                        try {
                            orgItem.deadline = simpleDateFormat.parse(datePick.getText().toString());
                        } catch (java.text.ParseException e) {

                        }
                        orgItem.treeLevel = 1;

                        doc.rootItems.add(orgItem);
                        doc.items.add(orgItem);
                        doc.file.write(doc);
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

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
