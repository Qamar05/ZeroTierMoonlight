package net.antplay.zerotiermoonlight.model;


import net.antplay.zerotiermoonlight.model.type.NetworkStatus;
import net.antplay.zerotiermoonlight.model.type.NetworkType;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class NetworkConfig {
    @Id
    private Long id;

    @Convert(converter = NetworkTypeConverter.class, columnType = Integer.class)
    private NetworkType type;

    @Convert(converter = NetworkStatusConverter.class, columnType = Integer.class)
    private NetworkStatus status;

    private String mac;

    private String mtu;

    private boolean broadcast;

    private boolean bridging;

    private boolean routeViaZeroTier;

    private boolean useCustomDNS;

    private int dnsMode;

    @ToMany(referencedJoinProperty = "networkId")
    private List<AssignedAddress> assignedAddresses;

    @ToMany(referencedJoinProperty = "networkId")
    private List<DnsServer> dnsServers;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1627972760)
    private transient NetworkConfigDao myDao;

    @Generated(hash = 1535887363)
    public NetworkConfig(Long id, NetworkType type, NetworkStatus status, String mac, String mtu,
            boolean broadcast, boolean bridging, boolean routeViaZeroTier, boolean useCustomDNS,
            int dnsMode) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.mac = mac;
        this.mtu = mtu;
        this.broadcast = broadcast;
        this.bridging = bridging;
        this.routeViaZeroTier = routeViaZeroTier;
        this.useCustomDNS = useCustomDNS;
        this.dnsMode = dnsMode;
    }

    @Generated(hash = 850630533)
    public NetworkConfig() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NetworkType getType() {
        return this.type;
    }

    public void setType(NetworkType type) {
        this.type = type;
    }

    public NetworkStatus getStatus() {
        return this.status;
    }

    public void setStatus(NetworkStatus status) {
        this.status = status;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMtu() {
        return this.mtu;
    }

    public void setMtu(String mtu) {
        this.mtu = mtu;
    }

    public boolean getBroadcast() {
        return this.broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean getBridging() {
        return this.bridging;
    }

    public void setBridging(boolean bridging) {
        this.bridging = bridging;
    }

    public boolean getRouteViaZeroTier() {
        return this.routeViaZeroTier;
    }

    public void setRouteViaZeroTier(boolean routeViaZeroTier) {
        this.routeViaZeroTier = routeViaZeroTier;
    }

    public boolean getUseCustomDNS() {
        return this.useCustomDNS;
    }

    public void setUseCustomDNS(boolean useCustomDNS) {
        this.useCustomDNS = useCustomDNS;
    }

    public int getDnsMode() {
        return this.dnsMode;
    }

    public void setDnsMode(int dnsMode) {
        this.dnsMode = dnsMode;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1342378014)
    public List<AssignedAddress> getAssignedAddresses() {
        if (assignedAddresses == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AssignedAddressDao targetDao = daoSession.getAssignedAddressDao();
            List<AssignedAddress> assignedAddressesNew = targetDao
                    ._queryNetworkConfig_AssignedAddresses(id);
            synchronized (this) {
                if (assignedAddresses == null) {
                    assignedAddresses = assignedAddressesNew;
                }
            }
        }
        return assignedAddresses;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1705851723)
    public synchronized void resetAssignedAddresses() {
        assignedAddresses = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1958390114)
    public List<DnsServer> getDnsServers() {
        if (dnsServers == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DnsServerDao targetDao = daoSession.getDnsServerDao();
            List<DnsServer> dnsServersNew = targetDao._queryNetworkConfig_DnsServers(id);
            synchronized (this) {
                if (dnsServers == null) {
                    dnsServers = dnsServersNew;
                }
            }
        }
        return dnsServers;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 980018091)
    public synchronized void resetDnsServers() {
        dnsServers = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1093510048)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getNetworkConfigDao() : null;
    }


    public static class NetworkTypeConverter implements PropertyConverter<NetworkType, Integer> {
        public NetworkType convertToEntityProperty(Integer num) {
            if (num == null) {
                return null;
            }
            return NetworkType.fromInt(num);
        }

        public Integer convertToDatabaseValue(NetworkType networkType) {
            if (networkType == null) {
                return null;
            }
            return networkType.toInt();
        }
    }

    public static class NetworkStatusConverter implements PropertyConverter<NetworkStatus, Integer> {
        public NetworkStatus convertToEntityProperty(Integer num) {
            if (num == null) {
                return null;
            }
            return NetworkStatus.fromInt(num);
        }

        public Integer convertToDatabaseValue(NetworkStatus networkStatus) {
            if (networkStatus == null) {
                return null;
            }
            return networkStatus.toInt();
        }
    }
}
