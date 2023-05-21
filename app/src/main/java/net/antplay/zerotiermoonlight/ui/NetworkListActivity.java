package net.antplay.zerotiermoonlight.ui;

import androidx.fragment.app.Fragment;

/**
 * Container activity for the Network List fragment
 */
public class NetworkListActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new NetworkListFragment();
    }
}
