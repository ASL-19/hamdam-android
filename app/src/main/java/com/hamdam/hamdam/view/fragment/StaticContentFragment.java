package com.hamdam.hamdam.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hamdam.hamdam.model.StaticFact;
import com.hamdam.hamdam.util.UtilWrapper;

/**
 * Fragment for displaying static content
 */
public class StaticContentFragment extends Fragment {
    private String titleText, bodyText;
    private static final String TITLE = "Title", BODY = "Body", TOPIC = "Topic";
    private StaticFact.TOPIC_TYPES topic;
    private int RESULT_CODE_BROWSE = 501;

    public StaticContentFragment() {
    }

    public static StaticContentFragment newInstance(String title, String body, StaticFact.TOPIC_TYPES topic) {
        StaticContentFragment fragment = new StaticContentFragment();
        fragment.titleText = title;
        fragment.bodyText = body;
        fragment.topic = topic;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            titleText = savedInstanceState.getString(TITLE);
            bodyText = savedInstanceState.getString(BODY);
            topic = StaticFact.TOPIC_TYPES.valueOf(savedInstanceState.getString(TOPIC));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().setStatusBarColor
                        (ContextCompat.getColor(getContext(), UtilWrapper.getStatusBarColor(topic)));
            }
        }
        int color = ContextCompat.getColor(getActivity(), UtilWrapper.getBorderColor(topic));

        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_single_fact_layout, container, false);
        UtilWrapper.setActionBar(getActivity(), titleText, color, Color.WHITE, false);

        TextView title = (TextView) view.findViewById(com.hamdam.hamdam.R.id.fragment_fact_title);
        TextView body = (TextView) view.findViewById(com.hamdam.hamdam.R.id.fragment_fact_body);
        title.setText(titleText);

        body.setText(bodyText, TextView.BufferType.SPANNABLE);
        body.setMovementMethod(LinkMovementMethod.getInstance());
        linkTextView(body);

        Button returnButton = (Button) view.findViewById(com.hamdam.hamdam.R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, this.titleText);
        outState.putString(BODY, this.bodyText);
        outState.putString(TOPIC, topic.name());
        super.onSaveInstanceState(outState);
    }

    // Parse URLs and set clickable with custom click handler.
    // This is used to notify users before they click on cleartext links.
    // Patterns.WEB_URL does not account for all modern types of URLs,
    // but there are very few (<10) URLs in this app, they are found in static
    // content, and are all captured by this pattern, so at this point a
    // custom regex is not needed.
    private void linkTextView(TextView textView) {
        SpannableString bodyText = (SpannableString) textView.getText();
        textView.setAutoLinkMask(0);
        Linkify.addLinks(bodyText, Patterns.WEB_URL, "");
        URLSpan[] spans =
                bodyText.getSpans(0, bodyText.length(), URLSpan.class);
        for (URLSpan span : spans) {
            setClickable(bodyText, span);
        }
    }

    /*
     * SpannableString and link parsing adapted from examples by Zane Claes and Mark Murphy
     * https://commonsware.com/blog/2013/10/23/linkify-autolink-need-custom-urlspan.html
     * https://stackoverflow.com/users/559301/zane-claes
     */
    protected void setClickable(SpannableString string, final URLSpan span) {
        int start = string.getSpanStart(span);
        int end = string.getSpanEnd(span);
        int flags = string.getSpanFlags(span);

        // Set click handler for links. If non-https links, display warning.
        ClickableSpan clickableLink = new ClickableSpan() {
            public void onClick(View view) {
                if (!span.getURL().startsWith("https://")) {
                    Context context = getContext();
                    new AlertDialog.Builder(context)
                            .setMessage(context.getString(com.hamdam.hamdam.R.string.insecure_site_warning))
                            .setCancelable(true)
                            .setPositiveButton(context.getString(com.hamdam.hamdam.R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            handleLinkClick(span.getURL());
                                            dialogInterface.dismiss();
                                        }
                                    })
                            .setNegativeButton(context.getString(com.hamdam.hamdam.R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setTitle(context.getString(com.hamdam.hamdam.R.string.insecure_site_title))
                            .create()
                            .show();
                } else {
                    handleLinkClick(span.getURL());
                }
            }
        };
        string.setSpan(clickableLink, start, end, flags);
        string.removeSpan(span);
    }

    private void handleLinkClick(String url) {
        Uri page = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, page);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, RESULT_CODE_BROWSE);
        }
    }

}
