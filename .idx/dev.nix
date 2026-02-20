{ pkgs, ... }: {
  channel = "stable-24.05";

  packages = [
    pkgs.jdk21
    pkgs.maven
  ];

  idx = {
    extensions = [
      "vscjava.vscode-java-pack"
      "vscjava.vscode-lombok"
    ];

    previews = {
      enable = false;
      previews = {
        web = {
          command = [ "mvn" "spring-boot:run" "-Dspring.profiles.active=home" ];
          manager = "web";
          port = 8080;
          visibility = "public";
          env = {
            PORT = "$PORT";
          };
        };
      };
    };
  };
}