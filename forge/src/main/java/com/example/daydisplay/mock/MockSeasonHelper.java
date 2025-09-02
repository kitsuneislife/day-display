package com.example.daydisplay.mock;

import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classe Mock para simulação do SereneSeasons durante o desenvolvimento.
 * Esta classe fornece uma implementação falsa de estações que pode ser usada
 * para testar a integração sem depender do mod real.
 */
public final class MockSeasonHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String[] SEASONS = {"Primavera", "Verão", "Outono", "Inverno"};
    
    /**
     * Simula a obtenção da estação atual com base no tempo do mundo.
     * @param world O mundo Minecraft
     * @return O nome da estação atual
     */
    public static String getCurrentSeason(Level world) {
        if (world == null) return "Estação desconhecida";
        
        // Simula estações baseadas no tempo do mundo (cada 10 dias muda a estação)
        long ticks = world.getGameTime();
        int day = (int)(ticks / 24000L); // dias totais
        
        int seasonIndex = (day / 10) % SEASONS.length;
        LOGGER.debug("Mock SereneSeasons: Dia {} - Estação: {}", day, SEASONS[seasonIndex]);
        
        return SEASONS[seasonIndex];
    }
}
