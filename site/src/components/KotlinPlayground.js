import React            from 'react';
// import KotlinPlayground from 'kotlin-playground'
import { findDOMNode }  from 'react-dom';

export class KPlayground extends React.Component {
    constructor(props) {
        super(props)
        this.mode = props.language ?? "kotlin"
        this.code = props.children
        this.ref  = React.createRef()
    }

    node
    parent

    componentDidMount() {
        this.node   = this.ref.current
        this.parent = findDOMNode(this).parentNode ?? null

        switch (true) {
            case !this.withinTabbedPanel(): this.convertToPlayground      (); break // Ok to load playground since parent should be visible
            case  this.parentHidden     (): this.startObservingHiddenState(); break

            // Delay since tabs sometimes start visible then get hidden before playground load finishes
            default: setTimeout(() => {
                switch (true) {
                    case !this.parentHidden(): this.convertToPlayground      (); break // Ok to load playground since parent should be visible
                    default                  : this.startObservingHiddenState()        // Tab was hidden though it began visible, so wait to unhide before loading
                }
            })
        }
    }

    convertToPlayground() {
        this.node.className = "kotlin-playground"
        KotlinPlayground(`.${this.node.className}`)
    }

    startObservingHiddenState() {
        let observer = new MutationObserver((mutationList) => {
            mutationList.forEach((mutation) => {
                if (mutation.type === "attributes" && mutation.attributeName === "hidden" && !mutation.target.hidden) {
                    this.convertToPlayground()
                    observer.disconnect()
                }
            })
        })

        observer.observe(this.parent, {
            attributeFilter: ["hidden"],
            childList      : false
        });
    }

    parentHidden() {
        return this.parent.hasAttribute("hidden")
    }

    withinTabbedPanel() {
        return this.parent.getAttribute("role") === "tabpanel"
    }

    render() {
        return<code theme="darcula" mode={this.mode} data-highlight-only ref={this.ref}>
            {this.code}
        </code>
    }
}
