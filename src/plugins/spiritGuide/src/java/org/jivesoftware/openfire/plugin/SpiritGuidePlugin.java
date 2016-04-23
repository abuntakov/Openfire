package org.jivesoftware.openfire.plugin;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.ConflictException;
import org.jivesoftware.openfire.muc.ForbiddenException;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.spi.LocalMUCRole;
import org.jivesoftware.openfire.plugin.spiritguide.Bot;
import org.jivesoftware.openfire.plugin.spiritguide.BotMessage;
import org.jivesoftware.openfire.plugin.spiritguide.EventsBot;
import org.jivesoftware.openfire.plugin.spiritguide.Translater;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Spirit Guide plugin.
 * 
 * @author Alexander Buntakov
 */
public class SpiritGuidePlugin implements Plugin, PacketInterceptor {

    public static final String SG_MESSAGE_TYPE_ATTR = "sgType";

    public enum SGMessageType {
        DEFAULT, BOT_ANSWER, COMMAND_FOR_BOT, ANSWERED_BY_BOT, IGNORE_BOT,
    }

	private static final Logger Log = LoggerFactory.getLogger(SpiritGuidePlugin.class);

    private InterceptorManager interceptorManager;
    private MessageRouter messageRouter;
    private MultiUserChatManager mucManager;

    private String domainFrom;

    private final List<Bot> bots;

    private Translater translater;

    private MUCRoom room;

    public SpiritGuidePlugin() {
        bots = new ArrayList<Bot>();
        bots.add(new EventsBot());

        translater = new Translater();
        interceptorManager = InterceptorManager.getInstance();
        domainFrom = new JID(XMPPServer.getInstance().getServerInfo().getXMPPDomain()).getDomain();
        messageRouter = XMPPServer.getInstance().getMessageRouter();

        String botName = "events_bot@" + domainFrom;
        room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom("hotel");
        try {
            room.addMember(new JID(botName), null, room.getRole());
        } catch (ForbiddenException | ConflictException e) {
            Log.error("Could not add member", e);
        }
    }

    public void initializePlugin(PluginManager pManager, File pluginDirectory) {
        // register with interceptor manager
        interceptorManager.addInterceptor(this);
    }

    public void destroyPlugin() {
        // unregister with interceptor manager
        interceptorManager.removeInterceptor(this);
    }

    public void interceptPacket(Packet packet, Session session, boolean read,
            boolean processed) throws PacketRejectedException {

        if (isValidTargetPacket(packet, read, processed)) {

            Message original = (Message)packet;

            String lang = "ru";//original.getElement().attributeValue("sgLang");

            if(true/*lang != null && !lang.equalsIgnoreCase("en")*/) {
                String localizedMsg = translater.translate(original.getBody(), lang);

                original.getElement().addElement("locale").addText( original.getElement().elementText("body") );
                original.getElement().element("body").setText(localizedMsg);

                if (Log.isDebugEnabled()) {
                    Log.debug("Localized message: " + localizedMsg);
                }
            }




            if (Log.isDebugEnabled()) {
                Log.debug("Spirit Guide: intercepted packet:" + original.toString());
            }

            BotMessage botResponseMessage = getBotResponseMessage(original);
            if(botResponseMessage != null) {
                if (Log.isDebugEnabled()) {
                    Log.debug("Spirit Guide: send bot message:" + botResponseMessage.toString());
                }


                Message markedPackage = original.createCopy();
                markedPackage.getElement().addAttribute(SG_MESSAGE_TYPE_ATTR, SGMessageType.ANSWERED_BY_BOT.toString());

                messageRouter.route(markedPackage);
                sendBotMessage(botResponseMessage, original);

                throw new PacketRejectedException("Packet rejected with disallowed content!");
            }
        }
    }

    private BotMessage getBotResponseMessage(Message packet) {
        String clientMsg = packet.getBody();
        if (Log.isDebugEnabled()) {
            Log.debug("Spirit Guide: check client message:" + clientMsg);
        }

        for(Bot bot : bots) {
            BotMessage response = bot.getResponseMessage(clientMsg);
            if(response != null) {
                return response;
            }
        }

        return null;
    }

    private boolean isValidTargetPacket(Packet packet, boolean read, boolean processed) {
        return  !processed && read && (packet instanceof Message) && !(isIgnoreBot( packet.getElement() ));
    }

    private boolean isIgnoreBot(Element el) {

        if(el == null) {
            return true;
        }

        Attribute attr = el.attribute(SG_MESSAGE_TYPE_ATTR);
        return (attr != null) && (
                SGMessageType.IGNORE_BOT.toString().equals(attr.getValue())  ||
                SGMessageType.ANSWERED_BY_BOT.toString().equals(attr.getValue())
        );

    }

    private void sendBotMessage(BotMessage botMessage, Message origin) {
        JID clientId = origin.getType() == Message.Type.groupchat ? origin.getTo() : origin.getFrom();
        String botId = "hotel@conference." + domainFrom + "/" + botMessage.getBotName();

        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setFrom(botId);
        message.setTo(clientId);
        message.setBody(botMessage.getMessage());
        message.getElement().addAttribute(SG_MESSAGE_TYPE_ATTR, SGMessageType.BOT_ANSWER.toString());

        room.send(message);

    }
}