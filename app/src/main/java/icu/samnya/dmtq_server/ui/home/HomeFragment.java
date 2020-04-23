package icu.samnya.dmtq_server.ui.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import icu.samnya.dmtq_server.R;
import icu.samnya.dmtq_server.service.ServerService;


public class HomeFragment extends Fragment {

    private boolean serverStarted = false;

    private ServerService serverService;

    private Intent intent;

    private Button serverButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        intent = new Intent(getActivity(), ServerService.class);
        serverButton = root.findViewById(R.id.server_button);

        // Load server info to edit text
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String host = sharedPref.getString(getString(R.string.host_address), "localhost:3456");
        String asset = sharedPref.getString(getString(R.string.asset_address), "localhost:3456");

        EditText hostText = root.findViewById(R.id.host_edit_text);
        EditText assetText = root.findViewById(R.id.assets_edit_text);

        hostText.setText(host);
        assetText.setText(asset);

        // Start service
        getActivity().startService(intent);
        getActivity().bindService(intent, new ServerServiceConnection(), Context.BIND_AUTO_CREATE);

        updateStatus();

        // Register button event
        serverButton.setOnClickListener((event) -> {
            serverService.toggleServer();
            updateStatus();
        });

        Button hostEditButton = root.findViewById(R.id.host_edit_button);
        Button assetEditButton = root.findViewById(R.id.assets_edit_button);
        hostEditButton.setOnClickListener((event) -> {
            String text = hostText.getText().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.host_address), text);
            editor.apply();
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        });
        assetEditButton.setOnClickListener((event) -> {
            String text = assetText.getText().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.asset_address), text);
            editor.apply();
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        });


        return root;
    }

    private void updateStatus() {
        if(this.serverService==null) {
            Log.i("ServerStatus", "null");
            serverStarted = false;
        } else serverStarted = serverService.isAlive();
    }

    class ServerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serverService = ((ServerService.ServerBinder) service).getInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serverService = null;
        }
    }
}
