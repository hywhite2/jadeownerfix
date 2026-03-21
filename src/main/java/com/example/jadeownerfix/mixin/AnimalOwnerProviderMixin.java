package com.example.jadeownerfix.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import snownee.jade.addon.vanilla.AnimalOwnerProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.util.CommonProxy;

/**
 * Jade currently only requests server-side pet owner data when the client does
 * not have an owner UUID at all. On offline-mode servers (and in some cases on
 * online-mode servers for other players), the client often has the UUID but
 * cannot resolve that UUID to a name locally, so Jade falls back to "???".
 *
 * This mixin changes the request condition so Jade also asks the server for the
 * owner name when the UUID exists but the local username cache/profile lookup
 * still cannot resolve a name.
 */
@Mixin(value = AnimalOwnerProvider.class, remap = false)
public abstract class AnimalOwnerProviderMixin {

    @Inject(method = "shouldRequestData", at = @At("HEAD"), cancellable = true, remap = false)
    private void jadeownerfix$shouldRequestServerNameWhenLocalLookupFails(EntityAccessor accessor, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = accessor.getEntity();
        if (!(entity instanceof OwnableEntity ownable)) {
            return;
        }

        UUID ownerUuid = ownable.getOwnerUUID();
        if (ownerUuid == null) {
            cir.setReturnValue(true);
            return;
        }

        String locallyKnownName = CommonProxy.getLastKnownUsername(ownerUuid);
        if (locallyKnownName == null || locallyKnownName.isBlank()) {
            cir.setReturnValue(true);
        }
    }
}
