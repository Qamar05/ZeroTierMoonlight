package net.antplay.zerotiermoonlight.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zerotier.sdk.NodeStatus;
import com.zerotier.sdk.Version;
import com.zerotier.sdk.VirtualNetworkConfig;
import com.zerotier.sdk.VirtualNetworkType;


import net.antplay.zerotiermoonlight.R;
import net.antplay.zerotiermoonlight.ZeroTierMoonlightApplication;
import net.antplay.zerotiermoonlight.events.AfterJoinNetworkEvent;
import net.antplay.zerotiermoonlight.events.IsServiceRunningReplyEvent;
import net.antplay.zerotiermoonlight.events.IsServiceRunningRequestEvent;
import net.antplay.zerotiermoonlight.events.NetworkInfoReplyEvent;
import net.antplay.zerotiermoonlight.events.NetworkListCheckedChangeEvent;
import net.antplay.zerotiermoonlight.events.NetworkListReplyEvent;
import net.antplay.zerotiermoonlight.events.NetworkListRequestEvent;
import net.antplay.zerotiermoonlight.events.NodeDestroyedEvent;
import net.antplay.zerotiermoonlight.events.NodeIDEvent;
import net.antplay.zerotiermoonlight.events.NodeStatusEvent;
import net.antplay.zerotiermoonlight.events.NodeStatusRequestEvent;
import net.antplay.zerotiermoonlight.events.OrbitMoonEvent;
import net.antplay.zerotiermoonlight.events.StopEvent;
import net.antplay.zerotiermoonlight.events.VPNErrorEvent;
import net.antplay.zerotiermoonlight.events.VirtualNetworkConfigChangedEvent;
import net.antplay.zerotiermoonlight.model.AppNode;
import net.antplay.zerotiermoonlight.model.AssignedAddress;
import net.antplay.zerotiermoonlight.model.AssignedAddressDao;
import net.antplay.zerotiermoonlight.model.DaoSession;
import net.antplay.zerotiermoonlight.model.MoonOrbit;
import net.antplay.zerotiermoonlight.model.Network;
import net.antplay.zerotiermoonlight.model.NetworkConfig;
import net.antplay.zerotiermoonlight.model.NetworkConfigDao;
import net.antplay.zerotiermoonlight.model.NetworkDao;
import net.antplay.zerotiermoonlight.model.type.NetworkStatus;
import net.antplay.zerotiermoonlight.model.type.NetworkType;
import net.antplay.zerotiermoonlight.service.ZeroTierOneService;
import net.antplay.zerotiermoonlight.util.Constants;
import net.antplay.zerotiermoonlight.util.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.query.WhereCondition;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import lombok.ToString;
import lombok.val;

// TODO: clear up
public class NetworkListFragment extends Fragment {
    public static final String NETWORK_ID_MESSAGE = "com.zerotier.one.network-id";
    public static final String TAG = "NetworkListFragment";
    private final EventBus eventBus;
    private final List<Network> mNetworks = new ArrayList<>();
    boolean mIsBound = false;
    private long networkIDStr;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private ZeroTierOneService mBoundService;
    private final ServiceConnection mConnection = new ServiceConnection() {
        /* class com.zerotier.one.ui.NetworkListFragment.AnonymousClass1 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkListFragment.this.mBoundService = ((ZeroTierOneService.ZeroTierBinder) iBinder).getService();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            NetworkListFragment.this.mBoundService = null;
            NetworkListFragment.this.setIsBound(false);
        }
    };
    private VirtualNetworkConfig[] mVNC;
    private TextView nodeIdView;
    private TextView nodeStatusView;
    private TextView nodeClientVersionView;

    private View emptyView = null;
    final private RecyclerView.AdapterDataObserver checkIfEmptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        /**
         * Check if the list is empty
         */
        void checkIfEmpty() {
            if (emptyView != null && recyclerViewAdapter != null) {
                final boolean emptyViewVisible = recyclerViewAdapter.getItemCount() == 0;
                emptyView.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            }
        }
    };

    public NetworkListFragment() {
        Log.d(TAG, "Network List Fragment created");
        this.eventBus = EventBus.getDefault();
    }

    /* access modifiers changed from: package-private */
    public synchronized void setIsBound(boolean z) {
        this.mIsBound = z;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean isBound() {
        return this.mIsBound;
    }

    /* access modifiers changed from: package-private */
    public void doBindService() {
        if (!isBound()) {
            if (requireActivity().bindService(new Intent(getActivity(), ZeroTierOneService.class), this.mConnection, Context.BIND_NOT_FOREGROUND | Context.BIND_DEBUG_UNBIND)) {
                setIsBound(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doUnbindService() {
        if (isBound()) {
            try {
                requireActivity().unbindService(this.mConnection);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            } catch (Throwable th) {
                setIsBound(false);
                throw th;
            }
            setIsBound(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.eventBus.post(new NetworkListRequestEvent());
        // Initialize node and service status
        this.eventBus.post(new NodeStatusRequestEvent());
        this.eventBus.post(new IsServiceRunningRequestEvent());
    }

    @Override
    public void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.eventBus.unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_list, container, false);

        // Empty List Prompt
        this.emptyView = view.findViewById(R.id.no_data);

        // list, adapter settings
        this.recyclerView = view.findViewById(R.id.joined_networks_list);
        this.recyclerView.setClickable(true);
        this.recyclerView.setLongClickable(true);
        this.recyclerViewAdapter = new RecyclerViewAdapter(this.mNetworks);
        this.recyclerViewAdapter.registerAdapterDataObserver(checkIfEmptyObserver);
        this.recyclerView.setAdapter(this.recyclerViewAdapter);

        // Network status bar settings
        this.nodeIdView = view.findViewById(R.id.node_id);
        this.nodeStatusView = view.findViewById(R.id.node_status);
        this.nodeClientVersionView = view.findViewById(R.id.client_version);
        setNodeIdText();

        // load network data
        updateNetworkListAndNotify();

        // set add button
        FloatingActionButton fab = view.findViewById(R.id.fab_add_network);
        fab.setOnClickListener(parentView -> {
            Log.d(TAG, "Selected Join Network");
            startActivity(new Intent(getActivity(), JoinNetworkActivity.class));
        });

        return view;
    }

    /**
     * Send an Intent to connect to the specified network.
     * Start ZT service after VPN permission will be requested
     *
     * @param networkId 网络号
     */
    private void sendStartServiceIntent(long networkId) {
        Intent prepare = VpnService.prepare(getActivity());
        if (prepare != null) {
          /*  // 等待 VPN 授权后连接网络
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (activityResult) -> {
                var result = activityResult.getResultCode();
                Log.d(TAG, "Returned from AUTH_VPN");
                if (result == -1) {
                    // 授权
                    startService(networkId);
                } else if (result == 0) {
                    // 未授权
                    updateNetworkListAndNotify();
                }
            }).launch(prepare);*/
            resultLauncher.launch(prepare);
            return;
        }
        Log.d(TAG, "Intent is NULL.  Already approved.");
        startService(networkId);
    }

    val checkVPNAuth;
    public Intent prepare;
    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Connect to the network after waiting for VPN authorization
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (activityResult) -> {
            int result = activityResult.getResultCode();
            Log.d(TAG, "Returned from AUTH_VPN");
            if (result == -1) {
                // authorized
                startService(networkIDStr);
            } else if (result == 0) {
                // unauthorized
                updateNetworkListAndNotify();
            }
        });


    }

    @Override
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "NetworkListFragment.onCreate");
        super.onCreate(bundle);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.nodeStatusView.setText(R.string.status_offline);
        this.eventBus.register(this);
        this.eventBus.post(new IsServiceRunningRequestEvent());
        updateNetworkListAndNotify();
        this.eventBus.post(new NetworkListRequestEvent());
        this.eventBus.post(new NodeStatusRequestEvent());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        Log.d(TAG, "NetworkListFragment.onCreateOptionsMenu");
        menuInflater.inflate(R.menu.menu_network_list, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
        this.eventBus.post(new NodeStatusRequestEvent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuId = menuItem.getItemId();
        if (menuId == R.id.menu_item_settings) {
            Log.d(TAG, "Selected Settings");
            startActivity(new Intent(getActivity(), PrefsActivity.class));
            return true;
        } else if (menuId == R.id.menu_item_peers) {
            Log.d(TAG, "Selected peers");
            startActivity(new Intent(getActivity(), PeerListActivity.class));
            return true;
        } else if (menuId == R.id.menu_item_orbit) {
            Log.d(TAG, "Selected orbit");
            startActivity(new Intent(getActivity(), MoonOrbitActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private List<Network> getNetworkList() {
        DaoSession daoSession = ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession();
        daoSession.clear();
        return daoSession.getNetworkDao().queryBuilder().orderAsc(NetworkDao.Properties.NetworkId).build().forCurrentThread().list();
    }

    private void setNodeIdText() {
        List<AppNode> list = ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().getAppNodeDao().queryBuilder().build().forCurrentThread().list();
        if (!list.isEmpty()) {
            this.nodeIdView.setText(list.get(0).getNodeIdStr());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIsServiceRunningReply(IsServiceRunningReplyEvent event) {
        if (event.isRunning()) {
            doBindService();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkListReply(NetworkListReplyEvent networkListReplyEvent) {
        Log.d(TAG, "Got network list");
        this.mVNC = networkListReplyEvent.getNetworkList();
        updateNetworkListAndNotify();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkInfoReply(NetworkInfoReplyEvent networkInfoReplyEvent) {
        NetworkStatus networkStatus;
        Log.d(TAG, "Got Network Info");
        VirtualNetworkConfig networkInfo = networkInfoReplyEvent.getNetworkInfo();
        for (Network network : getNetworkList()) {
            if (network.getNetworkId() == networkInfo.getNwid()) {
                network.setConnected(true);
                if (!networkInfo.getName().isEmpty()) {
                    network.setNetworkName(networkInfo.getName());
                }
                NetworkDao networkDao = ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().getNetworkDao();
                NetworkConfigDao networkConfigDao = ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().getNetworkConfigDao();
                NetworkConfig networkConfig = network.getNetworkConfig();
                if (networkConfig == null) {
                    networkConfig = new NetworkConfig();
                    networkConfig.setId(network.getNetworkId());
                    networkConfigDao.insert(networkConfig);
                }
                network.setNetworkConfig(networkConfig);
                network.setNetworkConfigId(network.getNetworkId());
                networkDao.save(network);
                networkConfig.setBridging(networkInfo.isBridge());
                if (networkInfo.getType() == VirtualNetworkType.NETWORK_TYPE_PRIVATE) {
                    networkConfig.setType(NetworkType.PRIVATE);
                } else if (networkInfo.getType() == VirtualNetworkType.NETWORK_TYPE_PUBLIC) {
                    networkConfig.setType(NetworkType.PUBLIC);
                }
                switch (networkInfo.getStatus()) {
                    case NETWORK_STATUS_OK:
                        networkStatus = NetworkStatus.OK;
                        break;
                    case NETWORK_STATUS_ACCESS_DENIED:
                        networkStatus = NetworkStatus.ACCESS_DENIED;
                        break;
                    case NETWORK_STATUS_NOT_FOUND:
                        networkStatus = NetworkStatus.NOT_FOUND;
                        break;
                    case NETWORK_STATUS_PORT_ERROR:
                        networkStatus = NetworkStatus.PORT_ERROR;
                        break;
                    case NETWORK_STATUS_CLIENT_TOO_OLD:
                        networkStatus = NetworkStatus.CLIENT_TOO_OLD;
                        break;
                    case NETWORK_STATUS_AUTHENTICATION_REQUIRED:
                        networkStatus = NetworkStatus.AUTHENTICATION_REQUIRED;
                        break;
                    default:
                    case NETWORK_STATUS_REQUESTING_CONFIGURATION:
                        networkStatus = NetworkStatus.REQUESTING_CONFIGURATION;
                        break;
                }
                networkConfig.setStatus(networkStatus);
                StringBuilder macAddress = new StringBuilder(Long.toHexString(networkInfo.getMac()));
                while (macAddress.length() < 12) {
                    macAddress.insert(0, "0");
                }
                String sb = String.valueOf(macAddress.charAt(0)) +
                        macAddress.charAt(1) +
                        ':' +
                        macAddress.charAt(2) +
                        macAddress.charAt(3) +
                        ':' +
                        macAddress.charAt(4) +
                        macAddress.charAt(5) +
                        ':' +
                        macAddress.charAt(6) +
                        macAddress.charAt(7) +
                        ':' +
                        macAddress.charAt(8) +
                        macAddress.charAt(9) +
                        ':' +
                        macAddress.charAt(10) +
                        macAddress.charAt(11);
                networkConfig.setMac(sb);
                networkConfig.setMtu(Integer.toString(networkInfo.getMtu()));
                networkConfig.setBroadcast(networkInfo.isBroadcastEnabled());
                network.setNetworkConfigId(networkInfo.getNwid());
                network.setNetworkConfig(networkConfig);
                networkDao.save(network);
                networkConfigDao.save(networkConfig);
                ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().getAssignedAddressDao().queryBuilder().where(AssignedAddressDao.Properties.NetworkId.eq(networkInfo.getNwid()), new WhereCondition[0]).buildDelete().forCurrentThread().forCurrentThread().executeDeleteWithoutDetachingEntities();
                ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().clear();
                AssignedAddressDao assignedAddressDao = ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().getAssignedAddressDao();
                InetSocketAddress[] assignedAddresses = networkInfo.getAssignedAddresses();
                for (InetSocketAddress inetSocketAddress : assignedAddresses) {
                    InetAddress address = inetSocketAddress.getAddress();
                    short port = (short) inetSocketAddress.getPort();
                    AssignedAddress assignedAddress = new AssignedAddress();
                    String inetAddress = address.toString();
                    if (inetAddress.startsWith("/")) {
                        inetAddress = inetAddress.substring(1);
                    }
                    if (address instanceof Inet6Address) {
                        assignedAddress.setType(AssignedAddress.AddressType.IPV6);
                    } else if (address instanceof Inet4Address) {
                        assignedAddress.setType(AssignedAddress.AddressType.IPV4);
                    }
                    assignedAddress.setAddressBytes(address.getAddress());
                    assignedAddress.setAddressString(inetAddress);
                    assignedAddress.setPrefix(port);
                    assignedAddress.setNetworkId(networkConfig.getId());
                    assignedAddressDao.save(assignedAddress);
                }
            } else {
                network.setConnected(false);
                network.update();
            }
        }
        ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession().clear();
        updateNetworkListAndNotify();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVirtualNetworkConfigChanged(VirtualNetworkConfigChangedEvent event) {
        Log.d(TAG, "Got Network Info");
        VirtualNetworkConfig config = event.getVirtualNetworkConfig();

        // Toast 提示网络状态
        NetworkStatus status = NetworkStatus.fromVirtualNetworkStatus(config.getStatus());
        String networkId = com.zerotier.sdk.util.StringUtils.networkIdToString(config.getNwid());
        String message = null;
        switch (status) {
            case OK:
                message = getString(R.string.toast_network_status_ok, networkId);
                break;
            case ACCESS_DENIED:
                message = getString(R.string.toast_network_status_access_denied, networkId);
                break;
            case NOT_FOUND:
                message = getString(R.string.toast_network_status_not_found, networkId);
                break;
            case PORT_ERROR:
                message = getString(R.string.toast_network_status_port_error, networkId);
                break;
            case CLIENT_TOO_OLD:
                message = getString(R.string.toast_network_status_client_too_old, networkId);
                break;
            case AUTHENTICATION_REQUIRED:
                message = getString(R.string.toast_network_status_authentication_required, networkId);
                break;
            case REQUESTING_CONFIGURATION:
            default:
                break;
        }
        if (message != null) {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
        }

        // 触发网络信息更新
        this.onNetworkInfoReply(new NetworkInfoReplyEvent(config));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNodeID(NodeIDEvent nodeIDEvent) {
        setNodeIdText();
    }

    /**
     * 节点状态事件回调
     *
     * @param event 事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNodeStatus(NodeStatusEvent event) {
        NodeStatus status = event.getStatus();
        Version clientVersion = event.getClientVersion();
        // update online status
        if (status.isOnline()) {
            this.nodeStatusView.setText(R.string.status_online);
            if (this.nodeIdView != null) {
                this.nodeIdView.setText(Long.toHexString(status.getAddress()));
            }
        } else {
            setOfflineState();
        }
        // update client version
        if (this.nodeClientVersionView != null) {
            this.nodeClientVersionView.setText(StringUtils.toString(clientVersion));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNodeDestroyed(NodeDestroyedEvent event) {
        setOfflineState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVPNError(VPNErrorEvent event) {
        String errorMessage = event.getMessage();
        String message = getString(R.string.toast_vpn_error, errorMessage);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        updateNetworkListAndNotify();
    }

    private void setOfflineState() {
        TextView textView = this.nodeStatusView;
        if (textView != null) {
            textView.setText(R.string.status_offline);
        }
    }

    /**
     * 更新网络列表
     */
    private void updateNetworkList() {

        List<Network> networkList = getNetworkList();
        if (networkList != null) {
            // 设置连接状态
            for (Network oldNetwork : this.mNetworks) {
                for (Network network : networkList) {
                    if (oldNetwork.getNetworkId().equals(network.getNetworkId())) {
                        network.setConnected(oldNetwork.getConnected());
                        Log.d(TAG, "Old Network ID : " + oldNetwork.getNetworkIdStr() + " New Network ID : " + network.getNetworkIdStr());
                    }
                }
            }
            if (this.mVNC != null) {
                for (Network network : networkList) {
                    for (VirtualNetworkConfig virtualNetworkConfig : this.mVNC) {
                        network.setConnected(network.getNetworkId() == virtualNetworkConfig.getNwid());
                        Log.d(TAG, "Network ID : " + network.getNetworkIdStr() + " Virtual Network ID : " + virtualNetworkConfig.getName());
                    }
                }
            }

            // 更新列表
            this.mNetworks.clear();
            this.mNetworks.addAll(networkList);
            if (!mNetworks.isEmpty()) {
                Log.d(TAG, "Network Items : Network Str Id: " + mNetworks.get(0).getNetworkIdStr() + " Network Name: " + mNetworks.get(0).getNetworkName()
                        + " Network ID: " + mNetworks.get(0).getNetworkId() + " Network Config: " + mNetworks.get(0).getNetworkConfig()
                        + " Config ID: " + mNetworks.get(0).getNetworkConfigId() + " is Connected: " + mNetworks.get(0).getConnected()
                        + " Last Activated: " + mNetworks.get(0).getLastActivated() + " Default Route: " + mNetworks.get(0).getUseDefaultRoute());
            }
        }
    }

    /**
     * Update network list and UI
     */
    public void updateNetworkListAndNotify() {
        // update data
        updateNetworkList();
        // update list
        if (this.recyclerViewAdapter != null) {
            this.recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 启动 ZT 服务连接至指定网络
     *
     * @param networkId 网络号
     */
    private void startService(long networkId) {
        Intent intent = new Intent(getActivity(), ZeroTierOneService.class);
        intent.putExtra(ZeroTierOneService.ZT1_NETWORK_ID, networkId);
        doBindService();
        requireActivity().startService(intent);
    }

    /**
     * 停止 ZT 服务
     */
    private void stopService() {
        if (this.mBoundService != null) {
            this.mBoundService.stopZeroTier();
        }
        Intent intent = new Intent(requireActivity(), ZeroTierOneService.class);
        this.eventBus.post(new StopEvent());
        if (!requireActivity().stopService(intent)) {
            Log.e(TAG, "stopService() returned false");
        }
        doUnbindService();
        this.mVNC = null;
    }

    /**
     * 获得 Moon 入轨配置列表
     */
    private List<MoonOrbit> getMoonOrbitList() {
        DaoSession daoSession = ((ZeroTierMoonlightApplication) requireActivity().getApplication()).getDaoSession();
        return daoSession.getMoonOrbitDao().loadAll();
    }

    /**
     * 加入网络后事件回调
     *
     * @param event 事件
     */
    @Subscribe
    public void onAfterJoinNetworkEvent(AfterJoinNetworkEvent event) {
        Log.d(TAG, "Event on: AfterJoinNetworkEvent");
        // 设置网络 orbit
        List<MoonOrbit> moonOrbits = NetworkListFragment.this.getMoonOrbitList();
        this.eventBus.post(new OrbitMoonEvent(moonOrbits));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkListCheckedChangeEvent(NetworkListCheckedChangeEvent event) {
        SwitchCompat switchHandle = event.getSwitchHandle();
        boolean checked = event.isChecked();
        Network selectedNetwork = event.getSelectedNetwork();
        networkIDStr = selectedNetwork.getNetworkId();
        NetworkDao networkDao = ((ZeroTierMoonlightApplication) requireActivity().getApplication())
                .getDaoSession().getNetworkDao();
        if (checked) {
            // 启动网络
            Context context = requireContext();
            boolean useCellularData = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getBoolean(Constants.PREF_NETWORK_USE_CELLULAR_DATA, false);
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
                // 设备无网络
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(NetworkListFragment.this.getContext(), R.string.toast_no_network, Toast.LENGTH_SHORT).show();
                    switchHandle.setChecked(false);
                });
            } else if (useCellularData || !(activeNetworkInfo.getType() == 0)) {
                // 可以连接至网络
                // 先关闭所有现有网络连接
                for (Network network : this.mNetworks) {
                    if (network.getConnected()) {
                        network.setConnected(false);
                    }
                    network.setLastActivated(false);
                    network.update();
                }
                this.stopService();
                // 连接目标网络
                if (!this.isBound()) {
                    this.sendStartServiceIntent(selectedNetwork.getNetworkId());
                } else {
                    this.mBoundService.joinNetwork(selectedNetwork.getNetworkId());
                }
                Log.d(TAG, "Joining Network: " + selectedNetwork.getNetworkIdStr());
                selectedNetwork.setConnected(true);
                selectedNetwork.setLastActivated(true);
                networkDao.save(selectedNetwork);
            } else {
                // 移动数据且未确认
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(this.getContext(), R.string.toast_mobile_data, Toast.LENGTH_SHORT).show();
                    switchHandle.setChecked(false);
                });
            }
        } else {
            // 关闭网络
            Log.d(TAG, "Leaving Leaving Network: " + selectedNetwork.getNetworkIdStr());
            if (this.isBound() && this.mBoundService != null) {
                this.mBoundService.leaveNetwork(selectedNetwork.getNetworkId());
                this.doUnbindService();
            }
            this.stopService();
            selectedNetwork.setConnected(false);
            networkDao.save(selectedNetwork);
            this.mVNC = null;
        }
    }

    /**
     * 网络信息列表适配器
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private final List<Network> mValues;

        public RecyclerViewAdapter(List<Network> items) {
            this.mValues = items;
            Log.d(NetworkListFragment.TAG, "Created network list item adapter, Item count : " + items.size());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_network, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Network network = mValues.get(position);
            holder.mItem = network;
            // 设置文本信息
            holder.mNetworkId.setText(network.getNetworkIdStr());
            String networkName = network.getNetworkName();
            if (networkName != null && !networkName.isEmpty()) {
                holder.mNetworkName.setText(networkName);
            } else {
                holder.mNetworkName.setText(R.string.empty_network_name);
            }
            // 设置点击事件
            holder.mView.setOnClickListener(holder::onClick);
            // 设置长按事件
            holder.mView.setOnLongClickListener(holder::onLongClick);
            // 设置开关
            holder.mSwitch.setOnCheckedChangeListener(null);
            holder.mSwitch.setChecked(network.getConnected());
            holder.mSwitch.setOnCheckedChangeListener(holder::onSwitchCheckedChanged);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        @ToString
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNetworkId;
            public final TextView mNetworkName;
            public final SwitchCompat mSwitch;
            public Network mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mNetworkId = view.findViewById(R.id.network_list_network_id);
                mNetworkName = view.findViewById(R.id.network_list_network_name);
                mSwitch = view.findViewById(R.id.network_start_network_switch);
            }

            /**
             * 单击列表项打开网络详细页面
             */
            public void onClick(View view) {
                Log.d(NetworkListFragment.TAG, "ConvertView OnClickListener");
                Intent intent = new Intent(NetworkListFragment.this.getActivity(), NetworkDetailActivity.class);
                intent.putExtra(NetworkListFragment.NETWORK_ID_MESSAGE, this.mItem.getNetworkId());
                NetworkListFragment.this.startActivity(intent);
            }

            /**
             * 长按列表项创建弹出菜单
             */
            public boolean onLongClick(View view) {
                Log.d(NetworkListFragment.TAG, "ConvertView OnLongClickListener");
                PopupMenu popupMenu = new PopupMenu(NetworkListFragment.this.getActivity(), view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_network_item, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.menu_item_delete_network) {
                        // 删除对应网络
                        DaoSession daoSession = ((ZeroTierMoonlightApplication) NetworkListFragment.this.requireActivity().getApplication()).getDaoSession();
                        AssignedAddressDao assignedAddressDao = daoSession.getAssignedAddressDao();
                        NetworkConfigDao networkConfigDao = daoSession.getNetworkConfigDao();
                        NetworkDao networkDao = daoSession.getNetworkDao();
                        if (this.mItem != null) {
                            if (this.mItem.getConnected()) {
                                NetworkListFragment.this.stopService();
                            }
                            NetworkConfig networkConfig = this.mItem.getNetworkConfig();
                            if (networkConfig != null) {
                                List<AssignedAddress> assignedAddresses = networkConfig.getAssignedAddresses();
                                if (!assignedAddresses.isEmpty()) {
                                    for (AssignedAddress assignedAddress : assignedAddresses) {
                                        assignedAddressDao.delete(assignedAddress);
                                    }
                                }
                                networkConfigDao.delete(networkConfig);
                            }
                            networkDao.delete(this.mItem);
                        }
                        daoSession.clear();
                        // 更新数据
                        NetworkListFragment.this.updateNetworkListAndNotify();
                        return true;
                    } else if (menuItem.getItemId() == R.id.menu_item_copy_network_id) {
                        // 复制网络 ID
                        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(getString(R.string.network_id), this.mItem.getNetworkIdStr());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });
                return true;
            }

            /**
             * 点击开启网络开关
             */
            public void onSwitchCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                NetworkListFragment.this.eventBus.post(new NetworkListCheckedChangeEvent(
                        this.mSwitch,
                        isChecked,
                        this.mItem
                ));
            }
        }
    }
}
