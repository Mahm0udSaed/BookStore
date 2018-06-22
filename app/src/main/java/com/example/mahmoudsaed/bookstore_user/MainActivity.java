package com.example.mahmoudsaed.bookstore_user;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mahmoudsaed.bookstore_user.fragment.BookDetailsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BookAdapter.OnBookEventListener, NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerMyBooks;
    private BookAdapter adapter;
    private ArrayList<Book> books;
    private String lastBookKey = "";
    private GridLayoutManager gridLayoutManager;
    private boolean isLoadMore;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private Publisher publisher;
    private TextView tvPublisherName, tvPublisherEmail;
    private ImageView ivProfilePic;
    private LinearLayout linearProfile;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        books = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        recyclerMyBooks = findViewById(R.id.recycler_my_books);
        adapter = new BookAdapter(this, books);
        gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerMyBooks.setLayoutManager(gridLayoutManager);
        recyclerMyBooks.setAdapter(adapter);
        //getBooks();
        Toolbar toolbar = findViewById(R.id.toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        linearProfile = (LinearLayout) navigationView.getHeaderView(0);
        drawer = findViewById(R.id.drawer_layout);
        ivProfilePic = linearProfile.findViewById(R.id.iv_profile);
        tvPublisherName = linearProfile.findViewById(R.id.tv_publisher_name);
        tvPublisherEmail = linearProfile.findViewById(R.id.tv_publisher_email);
        recyclerMyBooks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Log.d("MainActivity","notEmpty");
                    int lastVisibleItemPosition = gridLayoutManager.findLastCompletelyVisibleItemPosition();

                    if (!isLoadMore && books != null && books.size() > 0 && lastVisibleItemPosition == books.size() - 1) {
                        isLoadMore = true;
                        lastBookKey = books.get(books.size() - 1).getId();
                        getBooks();
                    }
                }

            }
        });


        showPublisherInfoAndBooks();
    }


    private void showPublisherInfoAndBooks() {
        if (Utilities.isNetworkAvailable(this)) {
            Utilities.showLoadingDialog(this, Color.WHITE);
            getPublisherInfo();
            getBooks();
        } else {
            Snackbar.make(recyclerMyBooks, R.string.error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPublisherInfoAndBooks();
                        }
                    })
                    .setActionTextColor(Color.WHITE)
                    .show();
        }

    }


    private void getBooks() {
        //  Utilities.showLoadingDialog(this, Color.RED);
        FirebaseDatabase
                .getInstance()
                .getReference(Constants.REF_BOOK)
                .orderByKey()
                .startAt(lastBookKey)
                .limitToFirst(9)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Utilities.dismissLoadingDialog();

                        if (dataSnapshot.getChildrenCount() == 9) {
                            isLoadMore = false;
                        }

                        int i = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Book book = snapshot.getValue(Book.class);
                            if (book != null) {

                                if (!lastBookKey.isEmpty() && i == 0) {
                                    i++;
                                    continue;
                                }

                                adapter.addBook(book);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }


    private void getPublisherInfo() {
        if (user != null) {
            FirebaseDatabase
                    .getInstance()
                    .getReference(Constants.REF_PUBLISHER)
                    .child(user.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            publisher = dataSnapshot.getValue(Publisher.class);
                            if (publisher != null) {
                                tvPublisherEmail.setText(publisher.getEmail());
                                tvPublisherName.setText(publisher.getName());
                                Glide.with(MainActivity.this)
                                        .load(publisher.getImageUrl())
                                        .into(ivProfilePic);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public void onBookClick(Book book) {
        BookDetailsFragment.with(book).show(getSupportFragmentManager(), "");
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void editBook(Book book) {

    }


    private MenuItem searchMenuItem;
    SearchView searchView;
    private SimpleCursorAdapter cursorAdapter;
    private MatrixCursor matrixCursor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_books, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    matrixCursor = new MatrixCursor(new String[]{"_id", "title", "book"});
                    searchByBookTitle(newText);
                }
                return false;
            }
        });

        return true;
    }

    private void searchByBookTitle(String title) {
        FirebaseDatabase
                .getInstance()
                .getReference(Constants.REF_BOOK)
                .orderByChild(Constants.BOOK_TITLE)
                .startAt(title)
                .endAt(title + "\uf8ff")
                .limitToFirst(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int i = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Book book = snapshot.getValue(Book.class);
                            if (book != null) {
                                matrixCursor.addRow(new Object[]{++i, book.getTitle(), book});
                                // Log.d("SearchBOOK", book.getTitle());
                            }
                        }

                        cursorAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.item_suggestion
                                , matrixCursor, new String[]{"title"}, new int[]{R.id.tv_title}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                        searchView.setSuggestionsAdapter(cursorAdapter);

                        //Todo: Implement on Suggest book selected
                        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                            @Override
                            public boolean onSuggestionSelect(int position) {

                                return false;
                            }

                            @Override
                            public boolean onSuggestionClick(int position) {
                                return false;
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Log.d("SearchBOOK", databaseError.getMessage());
                    }
                });
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.group2) {

        } else if (id == R.id.Logout) {
            logout();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.sure)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        auth.signOut();
                        ActivityLauncher.openLoginActivity(MainActivity.this);
                        finish();
                    }
                }).show();
    }


}
