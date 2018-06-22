package com.example.mahmoudsaed.bookstore_user.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mahmoudsaed.bookstore_user.ActivityLauncher;
import com.example.mahmoudsaed.bookstore_user.Book;
import com.example.mahmoudsaed.bookstore_user.R;


public class BookDetailsFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private Book book;

    public BookDetailsFragment() {
    }

    public static BookDetailsFragment with(Book book) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ActivityLauncher.BOOK_KEY, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (Book) getArguments().getSerializable(ActivityLauncher.BOOK_KEY);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_details, container, false);

        TextView tvPrice = view.findViewById(R.id.tv_price);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        ImageView ivBook = view.findViewById(R.id.iv_book);
        Button btnReadBook = view.findViewById(R.id.btn_read_book);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(book.getTitle());

        Glide.with(getContext()).load(book.getImageUrl()).into(ivBook);

        tvPrice.setText(String.format("%s LE", book.getPrice()));
        tvDescription.setText(book.getDescription());

        btnReadBook.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        ActivityLauncher.openPDFViewerActivity(getContext(), book);
    }
}
