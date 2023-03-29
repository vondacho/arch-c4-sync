# Use cases

## Simple DSL read

To read a C4-model from a local file. As a result, the JSON representation of the model get printed in the console.

- Describe your C4-model into a model file.
    - using [Structurizr-DSL](https://github.com/structurizr/dsl/blob/master/docs/language-reference.md) format
    - using [Structurizr-JSON](https://structurizr.com/json) format
- Example `java -cp build/libs/arch-c4-sync.jar edu.obya.c4.appl.ReadAppKt doc/c4/dsl/sample.dsl`

## Simple DSL upload

To publish a C4-model file to a Structurizr cloud account. As a result, the remote C4-model can be visualized online on the Structurizr website.

- Retrieve the ID of your private Structurizr Workspace with your credentials (API-key, API-secret).
- Describe your C4-model into a model file.
    - using [Structurizr-DSL](https://github.com/structurizr/dsl/blob/master/docs/language-reference.md) format
    - using [Structurizr-JSON](https://structurizr.com/json) format
- Example `java -cp build/libs/arch-c4-sync.jar edu.obya.c4.appl.UploadAppKt doc/c4/dsl/sample.dsl <workspace-id> <api-key> <api-secret>`
- Visualize your C4-model online on [Structurizr](https://www.structurizr.com/workspace/<workspace-id>)

## Simple download

To download a remote C4-model from a Structurizr cloud account. As a result, the JSON representation of the model get printed in the console.

- Retrieve the ID of your private Structurizr Workspace with your credentials (API-key, API-secret).
- Execute `java -cp build/libs/arch-c4-sync.jar edu.obya.c4.appl.DownloadAppKt <workspace-id> <api-key> <api-secret>`

## Synchronization

To synchronize a remote C4-model hosted in a Structurizr cloud account with a local C4-model.

- Retrieve the ID of your private Structurizr Workspace with your credentials (API-key, API-secret).
- Describe your local C4-model into a model file.
    - using [Structurizr-DSL](https://github.com/structurizr/dsl/blob/master/docs/language-reference.md) format
    - using [Structurizr-JSON](https://structurizr.com/json) format
- Example `java -jar build/libs/arch-c4-sync.jar -download <workspace-id> <api-key> <api-secret> -dsl doc/c4/dsl/sample.dsl -upload`
- Visualize your remote C4-model online on [Structurizr](https://www.structurizr.com/workspace/<workspace-id>)

### Options

- **-download** `<workspace-id> <api-key> <api-secret>` downloads a C4-model from a remote Structurizr workspace
- **-upload** `[<workspace-id> <api-key> <api-secret>]` uploads the resulting C4-model to a remote Structurizr workspace
- **-dsl** `<list of paths>` activates the scanning of C4-model out of json or dsl files
- **-metadata** `<list of paths>` activates the scanning of C4-model out of `metatada.yaml` files
- **-naming** `<path>` replaces the default naming file by the given one
- **-viewcount** `<max count>` limits the number of views inside the resulting C4-model
- **-viewfilter** `<list of keys>` filters the views inside the resulting C4-model
- **-broker** imports queues and exchanges from main broker
- **-relationshipinfer** infers not specified relationships
- **-viewgen** `[<list of views>]` generates a set of standard views if these do no exist`
    - `landscape|system|services|service|ownership|components|deployment`
- **-viewenrich** add some external context to container and component views
- **-viewclean** remove some out of focus elements from container and component views
- **-sitegen** generates a website composed of standard web cards if these do no exist

### With Docker

```
docker run -v `pwd`:`pwd` -w `pwd` ghcr.io/vondacho/arch-c4-sync c4-sync -download <workspace-id> <api-key> <api-secret> -print
docker run -v `pwd`:`pwd` -w `pwd` ghcr.io/vondacho/arch-c4-sync c4-sync -download <workspace-id> <api-key> <api-secret> -dsl doc/c4/dsl -metadata metadata.yaml -broker -relationshipinfer -viewgen service -viewenrich -viewclean -upload
docker run -v `pwd`:`pwd` -w `pwd` ghcr.io/vondacho/arch-c4-sync c4-sync-default <workspace-id> <api-key> <api-secret>
```

### Contribute to documentation hub

- [Contribute](https://www.structurizr.com/share/38199/diagrams#c4-contribute)
- [Create diagrams using DSL](https://www.structurizr.com/share/38199/diagrams#c4-create-with-dsl)
- [Create diagrams using Editor](https://www.structurizr.com/share/38199/diagrams#c4-create-with-editor)

### CI pipeline use

Every project is a contributor from its local `doc/c4/dsl` directory.

```
#!groovy

node('default') {
    stage("Checkout") {
        checkout scm
    }
    stage("C4") {
        docker.withRegistry("https://ghcr.io") {
            withCredentials([usernamePassword(
                credentialsId: 'structurizr_username',
                usernameVariable: 'structurizr_api_key',
                passwordVariable: 'structurizr_api_secret')]) {

                docker.image("ghcr.io/vondacho/arch-c4-sync:latest").inside {
                    sh "c4-sync-default 38199 ${structurizr_api_key} ${structurizr_api_secret}"
                }
            }
        }
    }
}
```
