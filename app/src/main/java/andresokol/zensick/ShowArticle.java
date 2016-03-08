package andresokol.zensick;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowArticle extends AppCompatActivity {
    private JSONObject DATA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String temp = getIntent().getStringExtra("data");
        String pTitle= "", pBody = "", pAuthor = "";

        try {
            DATA = new JSONObject(temp);
            pTitle = DATA.getString("title");
            pBody = DATA.getString("body");
            pAuthor = DATA.getString("author");
        } catch (JSONException e) {
            Log.d("KIEK", e.toString());
        }

        LinearLayout parent = (LinearLayout) findViewById(R.id.article_container);

        TextView titleView = (TextView) parent.findViewById(R.id.title);
        titleView.setText(pTitle);

        TextView authorView = (TextView) parent.findViewById(R.id.author);
        authorView.setText(pAuthor);

        TextView lineView;

        String[] pLines = ParseLines(pBody);
        Spannable updatedLine;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (String Line : pLines) {
            updatedLine = PrepLine(Line);

            if (updatedLine == null)
            {
                Log.d("CRAP", "d");
                continue;
            }

            Log.d("STRING", updatedLine.toString());

            lineView = (TextView) inflater.inflate(R.layout.paragraph_template, null);
            lineView.setText(updatedLine);
            //lineView.setTextSize(16);
            //lineView.setTextColor(0xFF222222);

            parent.addView(lineView);
        }

        /*
        Log.d("STRING", PrepLine("   wwww    "));
        Log.d("STRING", PrepLine("   wwww"));
        Log.d("STRING", PrepLine("wwww    "));
        Log.d("STRING", PrepLine("wwww"));
        */
    }

    private String[] ParseLines(String inputLine) {
        String inputStr = inputLine;

        inputStr = inputStr.replaceAll("    \r\n", "\r\n\r\n");
        inputStr = inputStr.replaceAll("<c>", "\r\n\r\n");

        inputStr = inputStr.replaceAll("!\\[", "\r\n![");

        inputStr = inputStr.replaceAll("\r\n#", "\r\n\r\n#");
        Boolean flag = false;
        //Boolean flagImg = true;
        for (int i = 0; i < inputStr.length() - 1; i += 1) {
            if (flag) {
                if (inputStr.substring(i, i + 2).equals("\r\n")) {
                    inputStr = inputStr.substring(0, i+2) + "\r\n" + inputStr.substring(i + 2);
                    flag = false;
                }
            } else {
                if (inputStr.substring(i, i+1).equals("#")) flag = true;
            }/*
            if (flagImg) {
                if (inputStr.substring(i+1,i+2).equals(")")) {
                    flagImg = false;
                    inputStr = inputStr.substring(0, i + 2) + "\r\n" + inputStr.substring(i+2);
                } else {
                    if (inputStr.substring(i, i+2).equals("![")) flag = true;
                }
            }*/
        }

        inputStr = inputStr.replaceAll("\\u00A0", "");

        String[] parsed = inputStr.split("\r\n\r\n");

        for (String str : parsed) Log.d("PARSED", str);

        return parsed;
    }

    private Spannable PrepLine(String inputLine) {
        String cuttedLine = inputLine;
        cuttedLine = cuttedLine.replace("\r\n", " ");

        int len = cuttedLine.length();
        int cnt = 0;
        for (; cnt < len && cuttedLine.charAt(cnt) == ' '; cnt += 1);
        cuttedLine = cuttedLine.substring(cnt, len);

        len = cuttedLine.length();
        cnt = len - 1;
        for (; cnt > 0 && cuttedLine.charAt(cnt) == ' '; cnt -= 1) ;
        cuttedLine = cuttedLine.substring(0, cnt + 1);

//        Log.d("LASTCHAR", Integer.toString(cuttedLine.charAt(cuttedLine.length() - 1)));
//        Log.d("LASTCHAR", Integer.toString('\r'));

        if (cuttedLine.equals("")) return null;

        cuttedLine = "$$$$" + cuttedLine + "$$$$";
        int OFFSET = 4;
        Vector<Integer> bold = new Vector<>();
        Vector<Integer> italic = new Vector<>();
        for (int i = 0; i < cuttedLine.length() - 2; i += 1) {
            if (cuttedLine.substring(i+1, i+3).equals("**") && !cuttedLine.substring(i, i + 1).equals("\\")) {
                bold.add(i - OFFSET + 1);
                cuttedLine = cuttedLine.substring(0, i+1) + cuttedLine.substring(i + 3);
                i -= 1;
            }
            else if (cuttedLine.substring(i + 1, i + 2).equals("*") && !cuttedLine.substring(i, i + 1).equals("\\")) {
                italic.add(i - OFFSET + 1);
                cuttedLine = cuttedLine.substring(0, i + 1) + cuttedLine.substring(i + 2);
                i -= 1;
            }
        }

        int textsize = 0;

        while (cuttedLine.substring(OFFSET, OFFSET + 1).equals("#")) {
            textsize += 1;
            cuttedLine = cuttedLine.substring(0, OFFSET) + cuttedLine.substring(OFFSET + 1);
        }

        Spannable line = new SpannableString(cuttedLine.substring(OFFSET, cuttedLine.length() - OFFSET));
        if (textsize != 0) {
            line.setSpan(new RelativeSizeSpan(1f + (7 - textsize) * 0.1f), 0, cuttedLine.length() - 2 * OFFSET, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        for (int i = 0; i < bold.size(); i += 2) {
            line.setSpan(new StyleSpan(Typeface.BOLD), bold.elementAt(i), bold.elementAt(i+1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (int i = 0; i < italic.size(); i += 2) {
            line.setSpan(new StyleSpan(Typeface.ITALIC), italic.elementAt(i), italic.elementAt(i + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return line;
    }

}
