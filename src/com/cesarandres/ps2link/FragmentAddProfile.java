package com.cesarandres.ps2link;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.cesarandres.ps2link.base.BaseFragment;
import com.cesarandres.ps2link.module.ObjectDataSource;
import com.cesarandres.ps2link.soe.SOECensus;
import com.cesarandres.ps2link.soe.SOECensus.Game;
import com.cesarandres.ps2link.soe.SOECensus.Verb;
import com.cesarandres.ps2link.soe.content.CharacterProfile;
import com.cesarandres.ps2link.soe.content.Faction;
import com.cesarandres.ps2link.soe.content.response.Character_response;
import com.cesarandres.ps2link.soe.util.Collections.PS2Collection;
import com.cesarandres.ps2link.soe.util.QueryString;
import com.cesarandres.ps2link.soe.util.QueryString.QueryCommand;
import com.cesarandres.ps2link.soe.util.QueryString.SearchModifier;
import com.cesarandres.ps2link.soe.view.ProfileItemAdapter;
import com.cesarandres.ps2link.soe.volley.GsonRequest;
import com.google.gson.Gson;

/**
 * Created by cesar on 6/16/13.
 */
public class FragmentAddProfile extends BaseFragment implements OnClickListener {

	public static Bitmap vs_icon;
	public static Bitmap nc_icon;
	public static Bitmap tr_icon;

	public interface NameToSearchListener {
		void onProfileSelected(CharacterProfile profile);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		vs_icon = BitmapFactory.decodeResource(getActivity().getResources(),
				R.drawable.vs_icon);
		tr_icon = BitmapFactory.decodeResource(getActivity().getResources(),
				R.drawable.tr_icon);
		nc_icon = BitmapFactory.decodeResource(getActivity().getResources(),
				R.drawable.nc_icon);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View root = inflater.inflate(R.layout.fragment_add_profile, container,
				false);

		ListView listRoot = (ListView) root
				.findViewById(R.id.listFoundProfiles);
		listRoot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), ActivityProfile.class);
				intent.putExtra("profileId", ((CharacterProfile) myAdapter
						.getItemAtPosition(myItemInt)).getId());
				startActivity(intent);
			}
		});

		final ImageButton buttonCharacters = (ImageButton) root
				.findViewById(R.id.imageButtonSearchProfile);
		buttonCharacters.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				downloadProfiles();
			}
		});
		((TextView) root.findViewById(R.id.textViewFragmentTitle))
				.setText("Profiles Found");
		return root;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
	}

	private void downloadProfiles() {
		EditText searchField = (EditText) getActivity().findViewById(
				R.id.fieldSearchProfile);
		URL url;
		try {
			url = SOECensus.generateGameDataRequest(
					Verb.GET,
					Game.PS2,
					PS2Collection.CHARACTER,
					"",
					QueryString
							.generateQeuryString()
							.AddComparison(
									"name.first_lower",
									SearchModifier.STARTSWITH,
									searchField.getText().toString()
											.toLowerCase())
							.AddCommand(QueryCommand.LIMIT, "10"));

			Listener<Character_response> success = new Response.Listener<Character_response>() {
				@Override
				public void onResponse(Character_response response) {
					ListView listRoot = (ListView) getActivity().findViewById(
							R.id.listFoundProfiles);
					listRoot.setAdapter(new ProfileItemAdapter(getActivity(),
							response.getCharacter_list()));
					new UpdateTmpProfileTable().execute(response
							.getCharacter_list());

				}
			};

			ErrorListener error = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.equals(new Object());
				}
			};

			GsonRequest<Character_response> gsonOject = new GsonRequest<Character_response>(
					url.toString(), Character_response.class, null, success,
					error);
			ApplicationPS2Link.volley.add(gsonOject);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class UpdateTmpProfileTable extends
			AsyncTask<ArrayList<CharacterProfile>, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(
				ArrayList<CharacterProfile>... profiles) {
			int count = profiles[0].size();
			ArrayList<CharacterProfile> list = profiles[0];
			ObjectDataSource data = new ObjectDataSource(getActivity());
			data.open();
			int result = -5;
			for (int i = 0; i < count; i++) {
				if (data.getCharacter(list.get(i).getId(), true) == null) {
					data.insertCharacter(list.get(i), true);
				} else {
					result = data.updateCharacter(list.get(i), true);
				}
			}
			result++;
			data.close();
			return true;
		}
	}
}