package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.ullink.slack.simpleslackapi.SlackUser;

public class CmdContact implements CommandExecutor {

    private Mother mom;

    public CmdContact(Mother mom) {
        this.mom = mom;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean onCommand(SlackUser user, String[] args, String threadTimestamp) {
        if (args.length < 1) return false;

        String dstUserID = Util.getTaggedUserID(args[0]);
        SlackUser dstUser = mom.getSession().findUserById(dstUserID);

        if (dstUser == null) return false;

        Conversation conv = mom.findConversationByUserID(dstUserID);
        String directChanID =
                mom.getSession().openDirectMessageChannel(dstUser).getReply().getSlackChannel().getId();

        if (conv != null) {
            mom.startConversation(dstUser, directChanID, false);
            return true;
        }

        mom.startConversation(dstUser, directChanID, true);
        return true;
    }
}
