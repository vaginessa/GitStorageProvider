package fr.xgouchet.gitstorageprovider.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import fr.xgouchet.gitstorageprovider.GitApplication;
import fr.xgouchet.gitsp.R;
import fr.xgouchet.gitsp.oauth.OAuthAccount;
import fr.xgouchet.gitstorageprovider.core.account.AccountsManager;
import fr.xgouchet.gitstorageprovider.core.events.NavigationEvent;
import fr.xgouchet.gitstorageprovider.ui.adapters.CredentialsAdapter;
import fr.xgouchet.gitstorageprovider.utils.DoubleDeckerBus;

import static butterknife.ButterKnife.bind;

/**
 * This fragment displays the local credentials
 *
 * @author Xavier Gouchet
 */
public class CredentialsFragment extends Fragment {

    private DoubleDeckerBus mBus;

    @BindView(android.R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab)
    FloatingActionButton mFAB;

    private RecyclerView.Adapter mCredentialsAdapter;
    private AccountsManager mAccountsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the common event bus
        mBus = ((GitApplication) getActivity().getApplication()).getBus();
        mAccountsManager = ((GitApplication) getActivity().getApplication()).getAccountsManager();
        mCredentialsAdapter = new CredentialsAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ideal_local_repos, container, false);
        bind(this, root);

        // set recycler view layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mCredentialsAdapter);
        // TODO setup empty view

        // attach FAB to the recycler view
        mFAB.setOnClickListener(mFABOnClickListener);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // register our event handler
        // TODO mBus.register(mEventHandler);
    }

    @Override
    public void onPause() {
        super.onPause();

        // unregister our event handler
        // TODO mBus.unregister(mEventHandler);
    }

    private final View.OnClickListener mFABOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            List<OAuthAccount> accounts = mAccountsManager.getAccounts();
            if ((accounts == null) || (accounts.isEmpty())){
                Toast.makeText(getActivity(), "You need to add an account before creating credentials", Toast.LENGTH_SHORT).show();
                mBus.postOnUiThread(new NavigationEvent(NavigationEvent.NAV_ACCOUNT));
                return;
            }


        }
    };

}
