{
  description = "dvankley/quickstart";

  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        lib = nixpkgs.lib;
        version = if (self ? shortRev) then self.shortRev else "dirty";
      in {
        packages = rec {
          plaid-quickstart-backend = pkgs.callPackage (
            { maven, jre
            , makeWrapper }:

            maven.buildMavenPackage {
              pname = "plaid-quickstart-backend";
              inherit version;

              src = ./java;
              mvnHash = "sha256-KMHxL86UMck+QQKHdK36xde2JxQBDIiPo40YYRvDyAM=";
              nativeBuildInputs = [ makeWrapper ];

              installPhase = ''
                mkdir -p $out/bin
                mkdir -p $out/share/plaid-quickstart

                install -Dm644 config.yml                         $out/share/plaid-quickstart/config.yml
                install -Dm644 target/quickstart-1.0-SNAPSHOT.jar $out/share/plaid-quickstart/backend.jar

                makeWrapper ${jre}/bin/java $out/bin/plaid-quickstart-backend \
                  --add-flags "-jar $out/share/plaid-quickstart/backend.jar server $out/share/plaid-quickstart/config.yml"
              '';
            }
          ) {};

          plaid-quickstart-frontend = pkgs.callPackage (
            { buildNpmPackage
            , apiHost ? null }:

            buildNpmPackage {
              pname = "plaid-quickstart-frontend";
              inherit version;

              src = ./frontend;
              npmDepsHash = "sha256-1nVB7MsuluBPEC4mPiAhnp+DJJWtwhczZ+v7RIQvCF8=";
              npmRebuildFlags = [ "--ignore-scripts" ];

              installPhase = ''
                mkdir -p $out/dist
                cp -r build/* $out/dist
              '';
            }
          ) {};

          plaid-quickstart-frontend-test = plaid-quickstart-frontend.override {
            apiHost = "https://money.lolc.at";
          };
        };
      }
    );
}
