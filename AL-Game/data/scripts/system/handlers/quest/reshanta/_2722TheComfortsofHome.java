package quest.reshanta;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/*
 * author : Altaress
 */
public class _2722TheComfortsofHome extends QuestHandler
{
    private final static int    questId    = 2722;

    public _2722TheComfortsofHome()
    {
        super(questId);
    }
    
    @Override
    public void register()
    {
        qe.setNpcQuestData(278047).addOnQuestStart(questId);
        qe.setNpcQuestData(278047).addOnTalkEvent(questId);
        qe.setNpcQuestData(278056).addOnTalkEvent(questId);
        qe.setNpcQuestData(278126).addOnTalkEvent(questId);
        qe.setNpcQuestData(278043).addOnTalkEvent(questId);
        qe.setNpcQuestData(278032).addOnTalkEvent(questId);
        qe.setNpcQuestData(278037).addOnTalkEvent(questId);
        qe.setNpcQuestData(278040).addOnTalkEvent(questId);
        qe.setNpcQuestData(278068).addOnTalkEvent(questId);
        qe.setNpcQuestData(278066).addOnTalkEvent(questId);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env)
    {
        final Player player = env.getPlayer();
        int targetId = 0;
        if(env.getVisibleObject() instanceof Npc)
            targetId = ((Npc) env.getVisibleObject()).getNpcId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if(targetId == 278047)
        {
            if(qs == null || qs.getStatus() == QuestStatus.NONE)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
                else
                    return defaultQuestStartDialog(env);
            }
            else if(qs != null && qs.getStatus() == QuestStatus.START)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
                else if(env.getDialogId() == 1009)
                {
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestEndDialog(env);
            }
            else if(qs != null && qs.getStatus() == QuestStatus.REWARD)
            {
                return defaultQuestEndDialog(env);
            }
        }
        else if(targetId == 278056)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
                else if(env.getDialogId() == 10000)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278126)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
                else if(env.getDialogId() == 1009)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278043)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
                else if(env.getDialogId() == 10003)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278032)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 3)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
                else if(env.getDialogId() == 10004)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278037)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 4)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
                else if(env.getDialogId() == 10005)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278040)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 5)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);
                else if(env.getDialogId() == 10006)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278068)
        {
            if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 6)
            {
                if(env.getDialogId() == 25)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3398);
                else if(env.getDialogId() == 10255)
                {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(player, qs);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                else
                    return defaultQuestStartDialog(env);
            }
        }
        else if(targetId == 278066)
        {
            if(qs != null)
            {
                if(env.getDialogId() == 25 && qs.getStatus() == QuestStatus.START)
                    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
                else if(env.getDialogId() == 17)
                {
                    qs.setQuestVar(3);
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(player, qs);
                    return defaultQuestEndDialog(env);
                }
                else
                    return defaultQuestEndDialog(env);
            }
        }
        return false;
    }
}
