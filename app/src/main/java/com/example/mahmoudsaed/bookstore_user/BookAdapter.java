package com.example.mahmoudsaed.bookstore_user;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private ArrayList<Book> books;
    private ArrayList<Book> filterBooks;
    private Context context;
    private OnBookEventListener onBookActionListener;
    private static final int ITEM_DELETE = 1;
    private static final int ITEM_EDIT = 2;

    public BookAdapter(Context context, ArrayList<Book> books) {
        this.books = books;
        filterBooks = new ArrayList<>();
        filterBooks.addAll(books);
        this.context = context;
        if (context instanceof OnBookEventListener) {
            onBookActionListener = (OnBookEventListener) context;
        } else {
            throw new RuntimeException("Context must implement OnBookEventListener");
        }
    }

    public void addBook(Book book) {
        books.add(book);
        filterBooks.add(book);
        notifyDataSetChanged();
    }

    public void removeBook(Book book) {
        books.remove(book);
        filterBooks.remove(book);
        notifyDataSetChanged();
    }

    public void setBook(int position, Book book) {
        books.set(position, book);
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_book, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.book = filterBooks.get(position);
        holder.tvTitle.setText(holder.book.getTitle());
        holder.tvPrice.setText(String.valueOf(holder.book.getPrice()));
        Glide.with(context).load(holder.book.getImageUrl()).into(holder.ivBook);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBookActionListener.onBookClick(holder.book);
            }
        });

    }

    @Override
    public int getItemCount() {
        return filterBooks.size();
    }

    public void filter(String text) {
        filterBooks.clear();
        if (text.isEmpty()) {
            filterBooks.addAll(books);
        } else {
            for (Book book : books) {
                if (book.getTitle().toLowerCase().trim().startsWith(text.toLowerCase().trim())) {
                    filterBooks.add(book);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvPrice, tvTitle;
        ImageView ivBook;
        Book book;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvPrice = view.findViewById(R.id.tv_price);
            tvTitle = view.findViewById(R.id.tv_title);
            ivBook = view.findViewById(R.id.iv_book);
        }
    }

    public interface OnBookEventListener {
        void onBookClick(Book book);

        void editBook(Book book);
    }
}
