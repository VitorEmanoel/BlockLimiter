package me.vitoremnoel.blocklimiter.configuration;

import me.vitoremnoel.blocklimiter.BlockLimiter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class MainConfig {

    private Path defaultConfig;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private BlockLimiter blockLimiter;
    private ConfigurationNode config;
    private Logger logger;

    public MainConfig(Path defaultConfig, ConfigurationLoader<CommentedConfigurationNode> loader){
        this.defaultConfig = defaultConfig;
        this.blockLimiter = BlockLimiter.getPlugin();
        this.loader = loader;
        this.logger = blockLimiter.getLogger();
    }

    public ConfigurationNode getConfig() {
        return config;
    }

    public void setup(){
        try{
            config = loader.load();
            if(!defaultConfig.toFile().exists()){
                logger.info("Não foi achado a config!");
                setDefaultConfig();
            }
        }catch(IOException e){
            logger.error("Ocorreu um erro ao inicar a configuração.");
            logger.error("Erro: " + e.getMessage());
            logger.error("Causa: " + e.getCause().toString());
        }
    }

    public void setDefaultConfig(){
        try {
            logger.info("Criando configurações padrão!");
            config.getNode("BlockLimiter", "Geral", "permission").setValue("limit.blocks");
            config.getNode("BlockLimiter", "H2", "database").setValue("test");
            config.getNode("BlockLimiter", "H2", "user").setValue("root");
            config.getNode("BlockLimiter", "H2", "password").setValue("");

            config.getNode("BlockLimiter", "Messages", "break_limited_block").setValue("Você quebrou um bloco limitado, pode usa-lo em outro lugar.");
            config.getNode("BlockLimiter", "Messages", "unlimited_place").setValue("Você colocou um bloco limitado, mas para você não é limitado!");
            config.getNode("BlockLimiter", "Messages", "no_place").setValue("Você não tem pode colocar este bloco!");
            config.getNode("BlockLimiter", "Messages", "place_block").setValue("Você colocou um bloco limitado, você ainda pode colocar $hasBlock$");
            config.getNode("BlockLimiter", "Messages", "only_player").setValue("Somente players podem usar este comando!");
            config.getNode("BlockLimiter", "Messages", "no_has_permission").setValue("Você não tem permissão para usar este comando");
            config.getNode("BlockLimiter", "Messages", "empty_mainhand").setValue("Sua mão está vazia! Coloque o bloco que deseja limita nela!");
            config.getNode("BlockLimiter", "Messages", "no_permission").setValue("Você não colocou a permissão limitadora para este bloco.\n" +
                    "Use: /limitarbloco <permission>");
            config.getNode("BlockLimiter", "Messages", "its_not_block").setValue("Isto que você está segurando não é um bloco!");
            config.getNode("BlockLimiter", "Messages", "limite_sucess").setValue("Você limitou o bloco $bloco$");
            config.getNode("BlockLimiter", "Messages", "remove_limite").setValue("Você retirou os limites do bloco $bloco$");
            config.getNode("BlockLimiter", "Messages", "has_limited").setValue("Este bloco já esta limitado!");
            config.getNode("BlockLimiter", "Messages", "has_permission").setValue("Essa permissão já é usada em outro bloco!");
            config.getNode("BlockLimiter", "Messages", "no_limited").setValue("Este bloco não está limitado!");
            loader.save(config);
        }catch(IOException e){
            logger.error("Ocorreu um erro ao criar a config padrão!");
            logger.error("Erro: " + e.getMessage());
            logger.error("Causa: " + e.getCause().toString());
        }

    }

}
