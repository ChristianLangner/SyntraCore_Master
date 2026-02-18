{ pkgs, ... }: {
  channel = "stable-24.05";

  packages = [
    pkgs.jdk21    # Zwingend erforderlich für AyntraCore
    pkgs.maven    # Behebt deinen aktuellen Fehler im Terminal
  ];

  idx = {
    extensions = [
      "vscjava.vscode-java-pack"
      "vscjava.vscode-lombok" # Wichtig, um die 'cannot find symbol' Fehler bei @Builder zu lösen
    ];

    previews = {
      enable = true;
      previews = {
        web = {
          command = ["mvn" "spring-boot:run"];
          manager = "web";
          env = {
            PORT = "$PORT";
          };
        };
      };
    };
  };
}