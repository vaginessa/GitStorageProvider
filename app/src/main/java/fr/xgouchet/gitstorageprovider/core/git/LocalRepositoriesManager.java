package fr.xgouchet.gitstorageprovider.core.git;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.xgouchet.gitsp.git.LocalRepo;
import fr.xgouchet.gitstorageprovider.utils.actions.ActionQueueExecutor;
import fr.xgouchet.gitstorageprovider.utils.actions.AsyncActionListener;
import fr.xgouchet.gitstorageprovider.core.credentials.CredentialsManager;
import fr.xgouchet.gitstorageprovider.core.events.LocalRepositoriesChangedEvent;
import fr.xgouchet.gitstorageprovider.utils.DoubleDeckerBus;

/**
 * This manager provides interface to get information on local mLocalRepositories and perform actions over them
 *
 * @author Xavier Gouchet
 */
public class LocalRepositoriesManager {

    private static final String WORKSPACE_NAME = "workspace";
    private static final String TAG = LocalRepositoriesManager.class.getSimpleName();

    private final DoubleDeckerBus mBus;

    private final CredentialsManager mCredentialsManager;

    private final File mLocalWorkspace;

    private final ActionQueueExecutor mActionQueueExecutor = new ActionQueueExecutor();
    private final List<LocalRepo> mLocalRepositories = new ArrayList<>();


    public LocalRepositoriesManager(final @NonNull Context context,
                                    final @NonNull DoubleDeckerBus bus) {
        mBus = bus;

        mCredentialsManager = new CredentialsManager(context);

        // get the local workspaces
        mLocalWorkspace = new File(context.getFilesDir(), WORKSPACE_NAME);
        if (!mLocalWorkspace.exists()) {
            mLocalWorkspace.mkdirs();
        }
    }


    /**
     * Clones the remote project from the given uri to a local folder with the given name
     *
     * @param name the name of the local repository
     * @param uri  the remote (origin) url
     */
    public void cloneRepositoryAsync(String name, String uri) {
        CloneRepositoryAction.Input input = new CloneRepositoryAction.Input(uri,
                new File(mLocalWorkspace, name),
                mCredentialsManager);

        mActionQueueExecutor.queueAction(
                new CloneRepositoryAction(),
                input,
                mCloneActionListener);
    }

    /**
     * List all local mLocalRepositories, and start actions to query their states
     */
    public void listLocalRepositories() {
        Log.d(TAG, "listLocalRepositories");
        mActionQueueExecutor.queueAction(
                new VerifyLocalRepositoriesAction(),
                mLocalWorkspace,
                mVerifyActionListener);
    }

    /**
     * Sends an event to anyone interested that the local repos have changed
     */
    private void fireLocalRepositoriesChanged() {
        mBus.postOnUiThread(new LocalRepositoriesChangedEvent(mLocalRepositories));
    }

    /**
     * Listener for clone actions
     */
    private final AsyncActionListener<File, List<LocalRepo>>
            mVerifyActionListener = new AsyncActionListener<File, List<LocalRepo>>() {

        @Override
        public void onActionPerformed(final @Nullable List<LocalRepo> output) {
            for (LocalRepo candidates : output) {
                if (mLocalRepositories.contains(candidates)) {
                    mLocalRepositories.remove(candidates);
                }
                mLocalRepositories.add(candidates);
            }
            fireLocalRepositoriesChanged();
        }

        @Override
        public void onActionFailed(final @NonNull File input,
                                   final @NonNull Exception e) {
            Log.e(TAG, "Verify failed", e);
        }
    };

    /**
     * Listener for clone actions
     */
    private final AsyncActionListener<CloneRepositoryAction.Input, LocalRepo>
            mCloneActionListener = new AsyncActionListener<CloneRepositoryAction.Input, LocalRepo>() {
        @Override
        public void onActionPerformed(final @Nullable LocalRepo output) {
            if (mLocalRepositories.contains(output)) {
                mLocalRepositories.remove(output);
            }
            mLocalRepositories.add(output);
            fireLocalRepositoriesChanged();
        }

        @Override
        public void onActionFailed(final @NonNull CloneRepositoryAction.Input input,
                                   final @NonNull Exception e) {
            Log.e(TAG, "Clone failed", e);
            LocalRepo fakeRepo = new LocalRepo(input.getLocalPath());
            mActionQueueExecutor.queueAction(new DeleteRepositoryAction(), fakeRepo, mDeleteActionListener);
        }
    };

    /**
     * Listener for clone actions
     */
    private final AsyncActionListener<LocalRepo, Void>
            mDeleteActionListener = new AsyncActionListener<LocalRepo, Void>() {
        @Override
        public void onActionPerformed(final @Nullable Void output) {
            Log.d(TAG, "Delete successful");
        }

        @Override
        public void onActionFailed(final @NonNull LocalRepo input,
                                   final @NonNull Exception e) {
            Log.e(TAG, "Delete failed", e);
        }
    };
}
