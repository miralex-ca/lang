package com.online.languages.study.lang.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.online.languages.study.lang.DBHelper;
import com.online.languages.study.lang.NoteActivity;
import com.online.languages.study.lang.NoteEditActivity;
import com.online.languages.study.lang.R;
import com.online.languages.study.lang.adapters.NotesAdapter;
import com.online.languages.study.lang.adapters.OpenActivity;
import com.online.languages.study.lang.data.DataManager;
import com.online.languages.study.lang.data.NoteData;

import java.util.ArrayList;

import static com.online.languages.study.lang.Constants.ACTION_CREATE;
import static com.online.languages.study.lang.Constants.ACTION_UPDATE;
import static com.online.languages.study.lang.Constants.EXTRA_NOTE_ACTION;
import static com.online.languages.study.lang.Constants.EXTRA_NOTE_ID;
import static com.online.languages.study.lang.Constants.NOTES_LIST_ANIMATION;
import static com.online.languages.study.lang.Constants.SET_GALLERY_LAYOUT;
import static com.online.languages.study.lang.Constants.SET_GALLERY_LAYOUT_DEFAULT;
import static com.online.languages.study.lang.Constants.STATUS_DELETED;
import static com.online.languages.study.lang.Constants.STATUS_NEW;
import static com.online.languages.study.lang.Constants.STATUS_NORM;
import static com.online.languages.study.lang.Constants.STATUS_UPDATED;


public class NotesFragment extends Fragment {

    SharedPreferences appSettings;
    public String themeTitle;

    OpenActivity openActivity;


    String title = "Заметка ";
    String content = "Содержание заметки ";


    ArrayList<NoteData> notes;
    NotesAdapter adapter;
    RecyclerView recyclerView;
    DataManager dataManager;


    public NotesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_notes, container, false);

        // setHasOptionsMenu(true);

        openActivity = new OpenActivity(getActivity());
        dataManager = new DataManager(getActivity());

        notes = getNotes();

        recyclerView = rootview.findViewById(R.id.recycler_view);

        adapter = new NotesAdapter(getActivity(), notes);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(adapter);

        ViewCompat.setNestedScrollingEnabled(recyclerView, false);


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(final View view, final int position) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onNoteClick(view, position);
                    }
                }, 50);

            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));



        return rootview;

    }

    private void onNoteClick(View view, int position) {

        Intent i = new Intent(getActivity(), NoteActivity.class);
        i.putExtra(EXTRA_NOTE_ID, notes.get(position).id );
        startActivityForResult(i, 10);
        openActivity.pageTransition();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        updateList();

    }

    private void updateList() {


        checkList();


    }


    private void checkList() {

        if (!NOTES_LIST_ANIMATION) {
            ArrayList<NoteData> newNotes = getNotes();


            adapter = new NotesAdapter(getActivity(), newNotes);
            recyclerView.setAdapter(adapter);

        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkListAnimation();
                }
            }, 50);


        }

    }


    private void checkListAnimation() {


            ArrayList<NoteData> newNotes = getNotes();


            for (NoteData noteData: notes) noteData.status = STATUS_DELETED;
            for (NoteData newNote: newNotes)  newNote.status = STATUS_NEW;


            for (NoteData noteData: notes) {

                for (NoteData newNote: newNotes) {

                    if (newNote.id.equals(noteData.id)) {

                        newNote.status = STATUS_NORM;
                        noteData.status = STATUS_NORM;

                        if ( noteData.time_updated != newNote.time_updated) {

                            noteData.title = newNote.title;
                            noteData.content = newNote.content;
                            noteData.image = newNote.image;
                            noteData.time_updated = newNote.time_updated;

                            noteData.status = STATUS_UPDATED;

                        }

                        break;
                    }

                }
            }



            for(int i = 0; i < notes.size(); i++) {
                NoteData noteData = notes.get(i);

                if (noteData.status.equals(STATUS_UPDATED)) {
                    adapter.notifyItemChanged(i);
                }
                if (noteData.status.equals(STATUS_DELETED)) {
                    recyclerView.setMinimumHeight(recyclerView.getHeight());

                    notes.remove(i);
                    adapter.notifyItemRemoved(i);

                }
            }

            for(int i = 0; i < newNotes.size(); i++) {
                NoteData newNote = newNotes.get(i);

                if (newNote.status.equals(STATUS_NEW)) {
                    if (i > (notes.size()-1 )) {
                        notes.add(newNote);
                        adapter.notifyItemInserted(notes.size() -1 );

                    } else {
                        notes.add(i,newNote);
                        adapter.notifyItemInserted(i);

                    }
                }
            }


    }


    public void fabClick() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                newNote();
            }
        }, 50);

    }

    private void newNote() {
        Intent i = new Intent(getActivity(), NoteEditActivity.class);
        i.putExtra(EXTRA_NOTE_ID, "" );
        i.putExtra(EXTRA_NOTE_ACTION, ACTION_CREATE );

        startActivityForResult(i, 20);
    }


    private ArrayList<NoteData> getNotes() {

        return dataManager.getNotes();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_notes_list, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_note) {
            newNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public interface ClickListener{
        void onClick(View view,int position);
        void onLongClick(View view,int position);
    }
    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){
            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }




}