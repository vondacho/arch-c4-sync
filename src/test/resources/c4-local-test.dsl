workspace local.WORKSPACE.name local.workspace.description {
    model {
        p = person test.person "A test person external to the system" "Element,Person"
        enterprise local.workspace.model.enterprise.name {
            s = softwareSystem test.system "A test system" "Element,Software System" {
                ct = container test.container "A test container" "A test technology" "Element,Container,Microservice" {
                    cp = component test.component "A test component" "A test technology" "Element,Component"
                }
            }
        }
        p -> s "queries data" "technology" "Relationship,Query"
    }
    views {
        styles {
            element "Element" {
            }
        }
    }
}
