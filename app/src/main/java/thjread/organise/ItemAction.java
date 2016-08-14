package thjread.organise;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ItemAction extends DialogFragment {
    private static final String ARG_PATH = "path";

    private OrgItem item;

    public ItemAction() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param item OrgItem
     * @return A new instance of fragment ItemAction.
     */
    public static ItemAction newInstance(OrgItem item) {
        ItemAction fragment = new ItemAction();
        Bundle args = new Bundle();
        ArrayList<String> path = item.getPath();
        String pathString = "";
        for (int i=0; i<path.size(); ++i) {
            pathString += path.get(i);
            if (i != path.size()-1) {
                pathString += "/";
            }
        }
        args.putString(ARG_PATH, pathString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String path_string = getArguments().getString(ARG_PATH);
            List<String> path = Arrays.asList(path_string.split("/"));
            item = GlobalState.getFiles().getItem(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_item_action, container, false);

        final Button addChildButton = (Button) v.findViewById(R.id.task_action_add_child);
        addChildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChild();
            }
        });

        final Button addSiblingButton = (Button) v.findViewById(R.id.task_action_add_sibling);
        addSiblingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSibling();
            }
        });

        final Button editItemButton = (Button) v.findViewById(R.id.task_action_edit_task);
        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editItem();
            }
        });

        final Button deleteButton = (Button) v.findViewById(R.id.task_action_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });

        return v;
    }

    public void addChild() {
        Fragment f = AddTask.newInstance(item, item.document, null, null, false);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.add(f, null);
        ft.commit();
    }

    public void addSibling() {
        Fragment f = AddTask.newInstance(item.parent, item.document, item.child_number+1, null, false);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.add(f, null);
        ft.commit();
    }

    public void editItem() {
        Fragment f = AddTask.newInstance(item, item.document, null, true, false);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.add(f, null);
        ft.commit();
    }

    public void deleteItem() {
        if (item != null) {
            final AddTaskCallbackInterface callback = (AddTaskCallbackInterface) getActivity();
            AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle)
                    .setTitle("Confirm delete")
                    .setMessage("Delete \"" + item.title + "\"")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            callback.onItemChange(item, false, true);
                            item.document.deleteItem(item);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            dialog.show();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(this);
            ft.commit();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
