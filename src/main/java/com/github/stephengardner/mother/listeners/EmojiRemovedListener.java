package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.ReactionRemoved;
import com.ullink.slack.simpleslackapi.listeners.ReactionRemovedListener;

public class EmojiRemovedListener implements ReactionRemovedListener {

    private Mother mom;

    public EmojiRemovedListener(Mother mom) {
        this.mom = mom;
    }

    public void registerEvent() {
        mom.getSession().addReactionRemovedListener(this);
    }

    @Override
    public void onEvent(ReactionRemoved ev, SlackSession s) {
        if (s.sessionPersona().getId().equals(ev.getUser().getId())) return;

        Conversation conv = mom.findConversation(ev.getMessageID(), false);

        if (conv == null) return;

        conv.setReaction(
                ev.getMessageID(),
                ev.getEmojiName(),
                ev.getChannel().getId().equals(conv.getDirectChannelID()),
                true);
    }
}
