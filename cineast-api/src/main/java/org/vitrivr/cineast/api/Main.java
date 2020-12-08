package org.vitrivr.cineast.api;

import org.vitrivr.cineast.core.config.AestheticPredictorConfig;
import org.vitrivr.cineast.core.features.AestheticPredictorsConfigStorage;
import org.vitrivr.cineast.core.features.MainAestheticFeaturesPredictorInitializer;
import org.vitrivr.cineast.core.remote_predictor_communication.RemotePredictorCommunication;
import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.monitoring.PrometheusServer;
import org.vitrivr.cineast.standalone.util.CLI;

import java.util.HashMap;
import java.util.List;

public class Main {

  /**
   * Entrypoint for Cineast API application.
   *
   * @param args Program arguments.
   */
  public static void main(String[] args) {
    /* (Force) load application config. */
    if (args.length == 0) {
      System.out.println("No config path given, loading default config cineast.json");
      Config.loadConfig("cineast.json");
    }

    /* (Force) load application config. */
    if (args.length != 0) {
      if (Config.loadConfig(args[0]) == null) {
        System.err.println("Failed to load Cineast configuration from '" + args[0] + "'. Cineast API will shutdown...");
        System.exit(1);
      }
    }

    //Todo: Is there a better way to pass the config to the MainAestheticFeaturesPredictorInitializer?
   AestheticPredictorsConfigStorage.getInstance().setAestheticPredictorsConfig(Config.sharedConfig().getAestheticPredictorsConfig());
  //  MainAestheticFeaturesPredictorInitializer test = new MainAestheticFeaturesPredictorInitializer(Config.sharedConfig().getAestheticPredictorsConfig());
    //List<AestheticPredictorConfig> test = Config.sharedConfig().getAestheticPredictorsConfig();

    /* Start Cineast API endpoint. */
    APIEndpoint.getInstance().start();
    GRPCEndpoint.start();

    /* Initalize Monitoring */
    PrometheusServer.initialize();

    /* Start Cineast CLI in interactive mode (blocking). */
    CLI.start(CineastCli.class);

    /* This part is only reached when user enters exit/quit: Stops the Cineast API endpoint. */
    APIEndpoint.stop();
    GRPCEndpoint.stop();
    PrometheusServer.stopServer();
  }
}
