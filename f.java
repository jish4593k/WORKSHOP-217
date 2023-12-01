import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class DiscordBot extends ListenerAdapter {

    private static final String YOUR_ROLE = "Buyer";

    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault("discord bot token goes here")
                .addEventListeners(new DiscordBot())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ");
        if ("!gen".equals(command[0]) && command.length == 2) {
            generateKeys(event, Integer.parseInt(command[1]));
        } else if ("!redeem".equals(command[0]) && command.length == 2) {
            redeemKey(event, command[1]);
        }
    }

    private void generateKeys(MessageReceivedEvent event, int amount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("keys.txt", true))) {
            StringBuilder showKey = new StringBuilder();
            for (int i = 0; i < amount; i++) {
                String key = UUID.randomUUID().toString();
                showKey.append("\n").append(key);
                writer.write(key);
                writer.newLine();
            }

            if (showKey.length() == 37) {
                showKey = new StringBuilder(showKey.toString().replace("\n", ""));
                sendResponse(event.getChannel(), "Key: " + showKey.toString());
            } else if (showKey.length() > 37) {
                sendResponse(event.getChannel(), "Keys: " + showKey.toString());
            } else {
                sendResponse(event.getChannel(), "Something's wrong");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redeemKey(MessageReceivedEvent event, String key) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("used keys.txt", true))) {
            if (key.length() == 36) {
                if (isKeyUsed("used keys.txt", key)) {
                    sendEmbedResponse(event.getChannel(), "Invalid Key", "Inputed key has already been used!", 0xff0000);
                } else if (isKeyUsed("keys.txt", key)) {
                    event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRolesByName(YOUR_ROLE, true).get(0)).queue();
                    sendEmbedResponse(event.getChannel(), "Key Redeemed", "Key has now been redeemed", 0x008525);
                    writer.write(key);
                    writer.newLine();
                } else {
                    sendEmbedResponse(event.getChannel(), "Invalid Key", "Inputed key does not exist!", 0xff0000);
                }
            } else {
                sendEmbedResponse(event.getChannel(), "Invalid Key", "Inputed key has an invalid length!", 0xff0000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isKeyUsed(String fileName, String key) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendResponse(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    private void sendEmbedResponse(MessageChannel channel, String title, String value, int color) {
        MessageEmbed embed = new MessageEmbed(
                null,
                title,
                value,
                null,
                null,
                color,
                null,
                null
        );
        channel.sendMessage(embed).queue();
    }
}
