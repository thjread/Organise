package thjread.organise;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ItemAction extends DialogFragment {
    private static final String ARG_PATH = "path";
    private static final String ARG_DOC = "document";
    private static final String ARG_X = "x";
    private static final String ARG_Y = "y";

    private OrgItem item = null;
    private Org doc;

    private int x;
    private int y;

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
    public static ItemAction newInstance(OrgItem item, Org doc, int x, int y) {
        ItemAction fragment = new ItemAction();
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
        if (doc != null) {
            String title = doc.title;
            args.putString(ARG_DOC, title);
        }
        args.putInt(ARG_X, x);
        args.putInt(ARG_Y, y);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            String path_string = args.getString(ARG_PATH);
            if (path_string != null) {
                List<String> path = Arrays.asList(path_string.split("\n"));
                item = GlobalState.getFiles().getItem(path);
            }
            if (item != null) {
                doc = item.document;
            } else {
                String doc_title = args.getString(ARG_DOC);
                if (doc_title != null) {
                    doc = GlobalState.getFiles().getDocument(doc_title);
                }
            }
            x = args.getInt(ARG_X);
            y = args.getInt(ARG_Y);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

        if (item == null) {
            addSiblingButton.setVisibility(View.GONE);
            editItemButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);

            addChildButton.setText(getResources().getString(R.string.add_item));
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            v.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        int width = v.getWidth();
                        int height = v.getHeight();
                        float finalRadius = (float) Math.hypot(width, height) / 2.0f;
                        Animator anim = ViewAnimationUtils.createCircularReveal(v, width/2, height/2, 0, finalRadius);
                        anim.start();

                        if (x >= 0) {// set to -1 to disable animation
                            Path path = new Path();
                            int screenPos[] = {0, 0};
                            v.getLocationOnScreen(screenPos);
                            path.moveTo(x - screenPos[0], y - screenPos[1]);
                            path.lineTo(left, top);
                            ObjectAnimator mAnimator = ObjectAnimator.ofFloat(v, v.X, v.Y, path);
                            mAnimator.start();
                        }
                    }
                    v.removeOnLayoutChangeListener(this);
                }
            });
        }

        return v;
    }

    public void addChild() {
        Fragment f = AddTask.newInstance(item, doc, null, null, false);
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
        Fragment f = AddTask.newInstance(item, doc, null, true, false);
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
