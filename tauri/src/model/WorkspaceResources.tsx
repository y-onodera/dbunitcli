export type WorkspaceResources = {
    parameterList: ParameterListBuilder;
    resources: ResourcesSettings;
}
export type ParameterListBuilder = {
    convert: string[];
    compare: string[];
    generate: string[];
    run: string[];
    parameterize: string[];
}
export class ParameterList {
    readonly convert: string[];
    readonly compare: string[];
    readonly generate: string[];
    readonly run: string[];
    readonly parameterize: string[];
    constructor(
        convert: string[]
        , compare: string[]
        , generate: string[]
        , run: string[]
        , parameterize: string[]
    ) {
        this.convert = convert;
        this.compare = compare;
        this.generate = generate;
        this.run = run;
        this.parameterize = parameterize
    }
    static create(): ParameterList {
        return new ParameterList([], [], [], [], [])
    }
    static from(builder: ParameterListBuilder): ParameterList {
        return new ParameterList(builder.convert, builder.compare, builder.generate, builder.run, builder.parameterize)
    }
    replace(command: string, menuList: string[]): ParameterList {
        return new ParameterList(
            command === "convert" ? menuList : this.convert
            , command === "compare" ? menuList : this.compare
            , command === "generate" ? menuList : this.generate
            , command === "run" ? menuList : this.run
            , command === "parameterize" ? menuList : this.parameterize
        )
    }
}
export type ResourcesSettings = {
    datasetSettings: []
}