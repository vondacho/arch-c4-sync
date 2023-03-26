# ${system.softwareSystem.name} system

${system.softwareSystem.description}

## API

## Services

<#list ${system.microservices}?chunk(1) as row>
<#list row as cell>${cell.name}</#list>
</#list>
