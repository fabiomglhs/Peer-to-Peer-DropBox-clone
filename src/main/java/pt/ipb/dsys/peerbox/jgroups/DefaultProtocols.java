package pt.ipb.dsys.peerbox.jgroups;

import com.google.common.collect.Lists;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class DefaultProtocols {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProtocols.class);

    public static List<Protocol> gossipRouter() throws UnknownHostException {
        List<Protocol> protocols = Lists.newArrayList();

        TUNNEL tunnel = new TUNNEL();
        try {
            InetAddress grAddress = InetAddress.getByName("gossip-router");
            logger.info("Found gossip router at {} (using it)", grAddress);

            tunnel.setGossipRouterHosts("gossip-router[12001]");
        } catch (UnknownHostException e) {
            tunnel.setGossipRouterHosts("127.0.0.1[12001]");
        }

        protocols.add(tunnel.setPortRange(5));
        protocols.add(new PING());
        protocols.add(new MERGE3());
        protocols.add(new FD_SOCK());
        protocols.add(new FD_ALL3());
        protocols.add(new VERIFY_SUSPECT().setTimeout(1500));
        protocols.add(new BARRIER());
        protocols.add(new NAKACK2().useMcastXmit(true));
        protocols.add(new UNICAST3());
        protocols.add(new STABLE().setDesiredAverageGossip(5000).setMaxBytes(4));
        protocols.add(new GMS().printLocalAddress(true).setJoinTimeout(1000));
        protocols.add(new UFC());
        protocols.add(new MFC());
        protocols.add(new FRAG2().setFragSize(60));
        protocols.add(new STATE_TRANSFER());




        return protocols;

    }

}
