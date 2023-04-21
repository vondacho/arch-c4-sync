workspace "*" "*" {
    model {
        impliedRelationships false

        enterprise "*" {
            # internal systems
            c4DocumentationSystem = softwareSystem "C4 documentation system" "A system that aggregates C4 information published by every services and publishes it to a Structurizr Cloud account" "edgelab,c4" {
                # containers
                c4Sync = container "c4Sync" "Downloads, reads, merges and publishes C4 models" "Kotlin" "edgelab,c4"
                goParser = container "c4Parser" "Extracts C3 documentation from Go code" "GoLang" "edgelab,c4"
                jarParser = container "jarParser" "Extracts C3 from annotated Java/Kotlin code" "Java/Kotlin" "edgelab,c4"
                c4Json = container "c4.json" "Describes a C4 model with views" "Structurizr" "file,c4"
                c4Dsl = container "c4.dsl" "Describes a C4 model with views" "Structurizr" "file,c4"
                c4Code = container "c4.code" "Source or compiled code" "Py/Go/Kt/Java" "file,c4"
                c4Image = container "c4.image" "Snapshot of one view" "PNG/SVG" "file,c4"
            }

            # internal persons
            devTeam = person "Development" ""
        }

        # external systems
        c4ConfluenceSystem = softwareSystem "C4 Confluence Cloud" "Confluence Cloud for EL" "c4" {
            # containers
            c4ConfluenceSpace = container "C4 corporate space" "Confluence Space for EL C4 documentation" "Confluence" "edgelab,c4"
        }
        c4StructurizrSystem = softwareSystem "C4 Structurizr Cloud" "Structurizr Cloud system" "c4" {
            # containers
            c4CorporateWorkspace = container "C4 corporate workspace" "Structurizr workspace for EL C4 documentation" "Structurizr" "edgelab,c4"
            c4PrivateWorkspace = container "C4 private workspace" "Structurizr workspace for private C4 documentation" "Structurizr" "edgelab,c4"
        }
        elSystem = softwareSystem "EL system" "" {
            jenkins = container "jenkins" "Continuous Integration Platform" "Jenkins" "edgelab,infrastructure" {
                elPipeline = component "EL-service-pipeline" "Any EL service pipeline" "Jenkins" "edgelab,infrastructure"
            }
        }
        githubSystem = softwareSystem "GitHub" "Code Versioning System" "infrastructure"

        # relationships
        devTeam -> c4CorporateWorkspace "explore / export"
        devTeam -> c4PrivateWorkspace "administrates" "" ""
        devTeam -> c4Sync "runs" "Docker run"
        devTeam -> c4Dsl "commits"
        devTeam -> c4Json "shares"
        devTeam -> githubSystem "pushes"

        githubSystem -> jenkins "triggers"
        jenkins -> c4Sync "runs" "Docker run"

        c4DocumentationSystem -> c4StructurizrSystem "downloads / uploads C4 model" "HTTPS/REST/ApiKey"
        c4DocumentationSystem -> c4ConfluenceSystem "produces web pages" "HTTPS/REST/Token"

        c4Sync -> c4CorporateWorkspace "uploads / downloads C4 model" "HTTPS/REST/ApiKey"
        c4Sync -> c4PrivateWorkspace "uploads / downloads C4 model" "HTTPS/REST/ApiKey"
        c4Sync -> jarParser "reads Structurizr annotations" "Classpath"
        c4Sync -> goParser "reads C4 information" "File"
        c4Sync -> c4ConfluenceSpace "produces web pages" "HTTPS/REST/Token"
        c4Sync -> c4Dsl "reads"

        goParser -> c4Code "parses"
        jarParser -> c4Code "scans"

        c4CorporateWorkspace -> c4Json "exports"
        c4CorporateWorkspace -> c4Image "exports"
        c4PrivateWorkspace -> c4Json "import / exports"
        c4PrivateWorkspace -> c4Image "exports"
    }

    # views
    views {
        systemContext c4DocumentationSystem "c4-system" "The landscape C4 documentation system" {
            include c4DocumentationSystem c4StructurizrSystem c4ConfluenceSystem
        }
        container c4DocumentationSystem "c4-sync" "Inside the C4 documentation system" {
            include c4Sync c4PrivateWorkspace c4CorporateWorkspace c4ConfluenceSpace goParser jarParser jenkins githubSystem devTeam
        }

        dynamic c4DocumentationSystem "c4-create-with-editor" "Create your own models and views using editor" {
            devTeam -> c4PrivateWorkspace "administrates"
            devTeam -> c4CorporateWorkspace "exports"
            c4CorporateWorkspace -> c4Json "exports"
            devTeam -> c4PrivateWorkspace "imports"
            c4PrivateWorkspace -> c4Json "imports"
            devTeam -> c4PrivateWorkspace "designs"
            devTeam -> c4PrivateWorkspace "exports"
            c4PrivateWorkspace -> c4Image "exports"
            c4PrivateWorkspace -> c4Json "exports"
            devTeam -> c4Json "shares"
        }

        dynamic c4DocumentationSystem "c4-create-with-dsl" "Create your own models and views using DSL" {
            devTeam -> c4PrivateWorkspace "administrates"
            devTeam -> c4Dsl "writes"
            devTeam -> c4Sync "runs"
            c4Sync -> c4Dsl "parses"
            c4Sync -> c4PrivateWorkspace "synchronizes"
            devTeam -> c4PrivateWorkspace "adjusts positions"
            devTeam -> c4Dsl "commits inside project"
            devTeam -> c4Dsl "shares"
        }

        container c4DocumentationSystem "c4-contribute" "Contribute to the EL software documentation" {
            include devTeam c4Dsl c4Code githubSystem jenkins c4Sync c4CorporateWorkspace goParser jarParser c4ConfluenceSpace
            animationStep devTeam c4Dsl githubSystem jenkins
            animationStep jenkins c4Sync
            animationStep c4Sync c4CorporateWorkspace
            animationStep c4Sync goParser jarParser c4Code
            animationStep c4Sync c4CorporateWorkspace c4ConfluenceSpace
        }
        
        branding {
            font "Open Sans"
        }

        styles {
            element "Element" {
                fontSize 25
            }
            element "Software System" {
                shape RoundedBox
                background #1168bd
                color #ffffff
            }
            element "Container" {
                shape Box
                background #438dd5
                color #ffffff
            }
            element "Component" {
                shape Box
                background #85bbf0
                color #000000
            }
            element "Person" {
                shape Person
                background #08427b
                color #ffffff
            }
            element "external-system" {
                shape RoundedBox
                background #575757
                color #ffffff
            }
            element "external-container" {
                background #7f7f7f
                color #ffffff
            }
            element "external-component" {
                background #b1b1b1
                color #000000
            }
            element "external-person" {
                background #363636
                color #ffffff
            }
            element "integrator" {
                background #08427b
            }
            element "database" {
                shape Cylinder
            }
            element "broker" {
                shape Pipe
            }
            element "queue" {
                shape Pipe
            }
            element "evooq" {
                background #ad134c
            }
            element "edgelab" {
                background #00d1bc
                color #000000
            }
            element "team" {
                background #dcdcdc
                color #000000
            }
            element "infrastructure" {
                background #dcdcdc
                color #000000
            }
            element "controller" {
                background #4f9eee
            }
            element "service" {
                background #66abf0
            }
            element "handler" {
                background #93bfec
            }
            element "listener" {
                background #b6d1ec
            }
            element "publisher" {
                background #b6d1ec
            }
            element "client" {
                background #b6d1ec
            }
            element "repository" {
                background #b6d1ec
            }
            element "file" {
                width  250
                background #ffffff
                color #000000
            }
            relationship "Relationship" {
                routing Curved
                dashed false
            }
            relationship "Asynchronous" {
                color #08427b
                dashed false
            }
            relationship "Synchronous" {
                color #888888
            }
            relationship "event" {
                color #08427b
            }
            relationship "command" {
                color #08427b
            }
            relationship "implements" {
                dashed true
            }
            relationship "imports" {
                dashed true
            }
        }

        themes https://static.structurizr.com/themes/amazon-web-services-2020.04.30/theme.json
    }
}
