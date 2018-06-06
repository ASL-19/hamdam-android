package com.hamdam.hamdam.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hamdam.hamdam.model.StaticFact;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Static content provider to load data
 */
public class StaticContentProviderImpl implements PresenterContracts.StaticContentProvider {
	private static final String TAG = "StaticContentProvider";
    private static PresenterContracts.StaticContentProvider mInstance = null;

    private StaticContentProviderImpl() {
    }

    @NonNull
    public static PresenterContracts.StaticContentProvider getInstance() {
        if (mInstance == null) {
            mInstance = new StaticContentProviderImpl();
        }
        return mInstance;
    }

    @Override
    @Nullable
    public ArrayList<StaticFact> loadStaticContent(BufferedReader filename, StaticFact.TOPIC_TYPES topic) {
        if (filename != null) {
            return parseContent(filename, topic);
        }
        return null;
    }

    @Nullable
    private ArrayList<StaticFact> parseContent(BufferedReader bufferedReader, StaticFact.TOPIC_TYPES topicType) {
        ArrayList<StaticFact> mData = new ArrayList<>();
        if (bufferedReader != null) {
            try {
                CSVReader reader = new CSVReader(bufferedReader, '|');
                List<String[]> allContent = reader.readAll();

                // Parse CSV file into fact objects
                for (String[] mEntry : allContent) {

                    // Facts have 3 columns: header, subheader, body
                    if (mEntry.length == 3) {
                        mData.add(new StaticFact.FactBuilder()
                                .heading(mEntry[0])
                                .subheading(mEntry[1])
                                .body(mEntry[2])
                                .setTopicType(topicType)
                                .setNewTopic(mEntry[0].length() > 0)
                                .build());
                    } else {
                        Log.e(TAG, "Unexpected entry format");
                    }
                }
            } catch (FileNotFoundException ex) {
                Log.e(TAG, ex.getMessage());
            } catch (IOException ex) {
                Log.e(TAG, "IOException: " + ex.getMessage());
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
            if (mData.size() > 0) {
                return mData;
            }
        }
        return null;
    }

}
