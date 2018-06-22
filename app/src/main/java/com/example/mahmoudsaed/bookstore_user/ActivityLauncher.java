package com.example.mahmoudsaed.bookstore_user;

import android.content.Context;
import android.content.Intent;

public final class ActivityLauncher {

    public static final String BOOK_KEY = "book";
    public static final String PUBLISHER_KEY = "publisher";

    public static void openLoginActivity(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
    }


    public static void openRegistrationActivity(Context context) {
        Intent i = new Intent(context, RegisterActivity.class);
        context.startActivity(i);
    }

    public static void openMyBooksActivity(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }


    public static void openPDFViewerActivity(Context context, Book book) {
        Intent i = new Intent(context, PDFViewerActivity.class);
        i.putExtra(BOOK_KEY, book);
        context.startActivity(i);
    }


}
