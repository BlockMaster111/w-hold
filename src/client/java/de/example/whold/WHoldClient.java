package de.example.whold;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class WHoldClient implements ClientModInitializer {
    private static long releaseAtNanos = 0L;
    private static boolean holdingForward = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                literal("w")
                    .then(argument("sekunden", FloatArgumentType.floatArg(0.05f, 3600.0f))
                        .executes(context -> {
                            float seconds = FloatArgumentType.getFloat(context, "sekunden");
                            startHolding(seconds);
                            context.getSource().sendFeedback(
                                Component.literal("Halte W für " + seconds + " Sekunden.")
                            );
                            return 1;
                        })
                    )
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (holdingForward && System.nanoTime() >= releaseAtNanos) {
                client.options.keyUp.setDown(false);
                holdingForward = false;
            }
        });
    }

    private static void startHolding(float seconds) {
        Minecraft client = Minecraft.getInstance();

        // Falls bereits ein Lauf aktiv ist, wird dessen Zeit durch den neuen Befehl ersetzt.
        client.options.keyUp.setDown(true);
        releaseAtNanos = System.nanoTime() + (long) (seconds * 1_000_000_000L);
        holdingForward = true;
    }
}
