package fr.xgouchet.gitstorageprovider.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xgouchet.gitstorageprovider.GitApplication;
import fr.xgouchet.gitstorageprovider.R;
import fr.xgouchet.gitstorageprovider.core.events.LocalRepositoriesChangedEvent;
import fr.xgouchet.gitstorageprovider.core.git.LocalRepositoriesManager;
import fr.xgouchet.gitstorageprovider.ui.adapters.LocalRepositoriesAdapter;
import fr.xgouchet.gitstorageprovider.utils.DoubleDeckerBus;

import com.melnykov.fab.FloatingActionButton;
import com.squareup.otto.Subscribe;

/**
 * This fragment displays the local mLocalRepositories, and allow local actions :
 * Clone, delete, pull, commit, push
 *
 * @author Xavier Gouchet
 */
public class LocalRepositoriesFragment extends Fragment {

    private static final String TAG = LocalRepositoriesFragment.class.getSimpleName();
    private DoubleDeckerBus mBus;

    @InjectView(android.R.id.list)
    RecyclerView mRecyclerView;
    @InjectView(R.id.fab)
    FloatingActionButton mFAB;

    private LocalRepositoriesAdapter mLocalRepositoriesAdapter;
    private LocalRepositoriesManager mLocalRepositoriesManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the common managers
        GitApplication app = (GitApplication) getActivity().getApplication();
        mBus = app.getBus();
        mLocalRepositoriesManager = app.getLocalRepositoriesManager();

        //
        mLocalRepositoriesAdapter = new LocalRepositoriesAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list_fab, container, false);
        ButterKnife.inject(this, root);

        // set recycler view layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mLocalRepositoriesAdapter);
        // TODO setup empty view

        // attach FAB to the recycler view
        mFAB.attachToRecyclerView(mRecyclerView);
        mFAB.setOnClickListener(mFABOnClickListener);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // register our event handler
        mBus.register(mEventHandler);

        // fetch all repos
        mLocalRepositoriesManager.listLocalRepositories();
    }

    @Override
    public void onPause() {
        super.onPause();

        // unregister our event handler
        mBus.unregister(mEventHandler);
    }

    private final View.OnClickListener mFABOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(), "Cloning Editors (need Credentials", Toast.LENGTH_SHORT).show();
            mLocalRepositoriesManager.cloneRepositoryAsync("Editors", "git@github.com:xgouchet/Editors.git");
        }
    };

    private Object mEventHandler = new Object() {
        @Subscribe
        public void onLocalRepositoriesChanged(LocalRepositoriesChangedEvent event) {
            Log.i(TAG, "onLocalRepositoriesChanged");
            mLocalRepositoriesAdapter.setLocalRepositories(event.getLocalRepositories());
        }
    };
}