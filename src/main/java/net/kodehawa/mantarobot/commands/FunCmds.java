package net.kodehawa.mantarobot.commands;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.commands.rpg.TextChannelGround;
import net.kodehawa.mantarobot.modules.Category;
import net.kodehawa.mantarobot.modules.CommandPermission;
import net.kodehawa.mantarobot.modules.Module;
import net.kodehawa.mantarobot.modules.SimpleCommand;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;

import java.util.Random;

public class FunCmds extends Module {

    public FunCmds() {
        super(Category.FUN);
        coinflip();
        dice();
    }

    private void coinflip() {
        super.register("coinflip", new SimpleCommand() {
            @Override
            protected void call(String[] args, String content, GuildMessageReceivedEvent event) {
                int times;
                if (args.length == 0 || content.length() == 0) times = 1;
                else {
                    try {
                        times = Integer.parseInt(args[0]);
                        if (times > 1000) {
                            event.getChannel().sendMessage(EmoteReference.ERROR + "Whoah there! The limit is 1,000 coinflips").queue();
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "You need to specify an Integer for the amount of " +
                                "repetitions").queue();
                        return;
                    }
                }

                final int[] heads = {0};
                final int[] tails = { 0 };
                doTimes(times, () -> {
                    if (new Random().nextBoolean()) heads[0]++;
                    else tails[0]++;
                });
                String flips = times == 1 ? "time" : "times";
                event.getChannel().sendMessage(EmoteReference.PENNY + " Your result from **" + times + "** " + flips + " yielded " +
                        "**" + heads[0] + "** heads and **" + tails[0] + "** tails").queue();
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Coinflip command")
                        .setDescription("Rolls a 6-sided dice with a defined number of repetitions")
                        .build();
            }
        });
    }

    private void dice() {
        super.register("roll", new SimpleCommand() {
            @Override
            protected void call(String[] args, String content, GuildMessageReceivedEvent event) {
                int roll;
                try {
                    roll = Integer.parseInt(args[0]);
                }
                catch (Exception e) {
                    roll = 1;
                }
                if (roll >= 100) roll = 100;
                event.getChannel().sendMessage(EmoteReference.DICE + "You got **" + diceRoll(roll) + "** with a total of **" + roll
                        + "** repetitions.").queue();
                TextChannelGround.of(event).dropItemWithChance(6, 5);
            }

            @Override
            public CommandPermission permissionRequired() {
                return CommandPermission.USER;
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Dice command")
                        .setDescription("Roll a 6-sided dice a specified number of times")
                        .build();
            }
        });
    }

    private synchronized int diceRoll(int repetitions) {
        int num = 0;
        int roll;
        for (int i = 0; i < repetitions; i++) {
            roll = new Random().nextInt(6) + 1;
            num = num + roll;
        }
        return num;
    }
}
