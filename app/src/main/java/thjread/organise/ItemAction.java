package thjread.organise;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ItemAction extends AppCompatActivity {
    private static final String ARG_PATH = "path";
    private static final String ARG_DOC = "document";

    private OrgItem item = null;
    private Org doc;

    private View container;

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
    public static Intent newInstance(Activity fromActivity, OrgItem item, Org doc) {
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
        Intent i = new Intent(fromActivity, ItemAction.class);
        i.putExtras(args);
        return i;
    }

    private Button addChildButton;
    private Button addSiblingButton;
    private Button editItemButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getIntent().getExtras();
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
        }

        setContentView(R.layout.activity_item_action);

        addChildButton = (Button) findViewById(R.id.task_action_add_child);
        addChildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChild();
            }
        });

        addSiblingButton = (Button) findViewById(R.id.task_action_add_sibling);
        addSiblingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSibling();
            }
        });

        editItemButton = (Button) findViewById(R.id.task_action_edit_task);
        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editItem();
            }
        });

        final Button deleteButton = (Button) findViewById(R.id.task_action_delete);
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

        container = findViewById(R.id.item_action_container);

        container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    int width = v.getWidth();
                    int height = v.getHeight();
                    float finalRadius = (float) Math.hypot(width, height) / 2.0f;
                    Animator anim = ViewAnimationUtils.createCircularReveal(v, width/2, height/2, 0, finalRadius);
                    anim.start();
                }
                v.removeOnLayoutChangeListener(this);
            }
        });

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return true;
            }
        });
    }

    private Bundle setupTransition(Button button) {
        Bundle b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionString = getResources().getString(R.string.add_task_transition);
            button.setTransitionName(transitionString);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this, button, transitionString);
            b = options.toBundle();
        } else {
            b = new Bundle();
        }
        return b;
    }

    public void addChild() {
        container.setVisibility(View.GONE);
        Bundle b = setupTransition(addChildButton);
        Intent i = AddTask.newInstance(this, item, doc, null, null, false);
        i.putExtras(b);
        ActivityCompat.startActivityForResult(this, i, ADD_TASK_RESULT, b);
    }

    public void addSibling() {
        container.setVisibility(View.GONE);
        Bundle b = setupTransition(addSiblingButton);
        Intent i = AddTask.newInstance(this, item.parent, item.document, item.child_number+1, null, false);
        i.putExtras(b);
        ActivityCompat.startActivityForResult(this, i, ADD_TASK_RESULT, b);
    }

    public void editItem() {
        container.setVisibility(View.GONE);
        Bundle b = setupTransition(editItemButton);
        Intent i = AddTask.newInstance(this, item, doc, null, true, false);
        i.putExtras(b);
        ActivityCompat.startActivityForResult(this, i, ADD_TASK_RESULT, b);
    }

    public void deleteItem() {
        container.setVisibility(View.GONE);
        if (item != null) {
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                    .setTitle("Confirm delete")
                    .setMessage("Delete \"" + item.title + "\"")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent data = new Intent();
                            data.putExtra(RESULT_ITEM_PATH, item.getPath());
                            data.putExtra(RESULT_IS_EDIT, false);
                            data.putExtra(RESULT_IS_DELETE, true);//Lets DocumentActivity actually delete
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    })
                    .create();

            dialog.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_TASK_RESULT) {
            setResult(resultCode, data);
            finish();
        }
    }

    private static final int ADD_TASK_RESULT = 1;

    public static final String RESULT_ITEM_PATH = "ITEM_PATH";
    public static final String RESULT_IS_EDIT = "IS_EDIT";
    public static final String RESULT_IS_DELETE = "IS_DELETE";
}
