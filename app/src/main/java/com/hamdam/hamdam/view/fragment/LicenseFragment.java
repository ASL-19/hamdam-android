package com.hamdam.hamdam.view.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hamdam.hamdam.util.UtilWrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;

import static com.hamdam.hamdam.Constants.LICENSE;

/**
 * App licensing information.
 */
public class LicenseFragment extends Fragment {
    private static final String TAG = "LicenseFragment";

    enum LicenseType {
        APACHE,
        MIT,
        GNU

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.hamdam.hamdam.R.layout.fragment_libraries, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor
                    (ContextCompat.getColor(getContext(), com.hamdam.hamdam.R.color.primary_dark));
        }

        UtilWrapper.setActionBar(getActivity(), getString(com.hamdam.hamdam.R.string.about), false);

        List<LibraryItem> libraries = getLibraries();
        final LibraryArrayAdapter arrayAdapter = new LibraryArrayAdapter
                (getContext(), com.hamdam.hamdam.R.layout.item_library_element, libraries);

        ListView mListView = (ListView) view.findViewById(com.hamdam.hamdam.R.id.license_list);
        mListView.setAdapter(arrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LicenseType licenseType = arrayAdapter.getItem(i).getLicenseType();
                String licenseText = null;
                switch (licenseType) {
                    case APACHE:
                        licenseText = getFullLicenseText("licenses/apache-2.0-license.txt");
                        break;
                    case GNU:
                        licenseText = getFullLicenseText("licenses/gpl-license.txt");
                        break;
                    case MIT:
                        licenseText = getFullLicenseText("licenses/mit-license.txt");
                        break;
                    default:
                        licenseText = arrayAdapter.getItem(i).getLicenseLink();
                }

                new AlertDialog.Builder(getContext(), com.hamdam.hamdam.R.style.HamdamTheme_CustomRomanDialogStyle)
                        .setTitle(arrayAdapter.getItem(i).getLicenseName())
                        .setMessage(licenseText)
                        .setNeutralButton(getString(com.hamdam.hamdam.R.string.dismiss), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });
        return view;
    }

    private @Nullable List<LibraryItem> getLibraries() {
        List<LibraryItem> mData = new ArrayList<LibraryItem>();
            try {
                BufferedReader bufferedReader = new BufferedReader
                        (new InputStreamReader(getActivity()
                                .getAssets().open(LICENSE), "utf-8"));
                CSVReader reader = new CSVReader(bufferedReader, ',');
                List<String[]> allContent = reader.readAll();

                // Parse CSV file into fact objects
                for (String[] mEntry : allContent) {
                     if (mEntry.length == 5) {
                        LicenseType type = getLicenseType(mEntry[3]);
                        mData.add(new LibraryItem
                                (mEntry[0], mEntry[1], mEntry[2],
                                        mEntry[3], mEntry[4], type));
                    } else {
                        Log.e(TAG, "Unexpected entry format");
                    }
                }
                reader.close();
            } catch (FileNotFoundException ex) {
                Log.e(TAG, ex.getMessage());
            } catch (IOException ex) {
                Log.e(TAG, "IOException " + ex.getMessage());
            }
            if (mData.size() > 0) {
                return mData;
            }
        return null;
    }

    private @Nullable LicenseType getLicenseType(String type) {
        if (type.contains("GNU")) {
            return LicenseType.GNU;
        } else if (type.contains("Apache")) {
            return LicenseType.APACHE;
        } else if (type.contains("MIT")) {
            return LicenseType.MIT;
        } else {
            return null;
        }
    }

    private @Nullable String getFullLicenseText(String relativeFilePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader
                    (getActivity().getAssets().open(relativeFilePath), "utf-8"));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(String.format(Locale.ENGLISH, "%s\n", line));
            }
            return builder.toString();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
        return null;
    }

    @TargetApi(17)
    private void setRomanTextLocale(TextView[] views) {
        for (TextView t : views) {
            t.setTextLocale(Locale.ENGLISH);
        }
    }

    private class LibraryArrayAdapter extends ArrayAdapter<LibraryItem> {
        private List<LibraryItem> mData;

        public LibraryArrayAdapter(Context context, int resource, List<LibraryItem> objects) {
            super(context, resource, objects);
            this.mData = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LibraryItem item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(com.hamdam.hamdam.R.layout.item_library_element, parent, false);
            }
            TextView name = (TextView) convertView.findViewById(com.hamdam.hamdam.R.id.name);
            TextView author = (TextView) convertView.findViewById(com.hamdam.hamdam.R.id.author);
            TextView site = (TextView) convertView.findViewById(com.hamdam.hamdam.R.id.site);
            TextView license = (TextView) convertView.findViewById(com.hamdam.hamdam.R.id.license);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setRomanTextLocale(new TextView[]{name, author, site, license});
            }

            name.setText(item.getName());
            author.setText(item.getAuthor());
            site.setText(item.getSite());
            license.setText(item.getLicenseName());

            return convertView;
        }

        @Override
        public int getCount() {
            return mData.size();
        }
    }

    // Android is frustrating.
    private String replaceFarsiNumbers(String original) {
        return original.replaceAll("۱","1")
                .replaceAll("۲","2")
                .replaceAll("۳","3")
                .replaceAll("۴", "4")
                .replaceAll("۵", "5")
                .replaceAll("۶", "6")
                .replaceAll("۷", "7")
                .replaceAll("۸", "8")
                .replaceAll("۹", "9")
                .replaceAll("۰", "0");
    }
    private class LibraryItem {
        private String name, author, site, licenseName, licenseLink;
        private LicenseType licenseType;
        private Locale locale;

        public LibraryItem(String name, String author, String site,
                           String licenseName, String licenseLink,
                           LicenseType licenseType) {
            this.name = name;
            this.author = author;
            this.site = site;
            this.licenseName = licenseName;
            this.licenseType = licenseType;
            this.licenseLink = licenseLink;
            this.locale = Locale.US;
        }

        public String getName() {
            return name;
        }

        public String getAuthor() {
            return author;
        }

        public String getLicenseName() {
            return licenseName;
        }

        public String getSite() {
            return site;
        }

        public LicenseType getLicenseType() {
            return licenseType;
        }

        public String getLicenseLink() {
            return licenseLink;
        }
    }

}
