import React from 'react';
import DocApps from './DocApps'
import styles from './DoodleCodeBlock.module.css'

// noinspection JSUnusedGlobalSymbols
export class DoodleCodeBlock extends React.Component {
  constructor(props) {
    super(props)
    if (props.args) {
      this.args = JSON.parse(props.args)
    }

    this.height       = props.height
    this.functionName = props.functionName
    this.ref = React.createRef()
  }

  componentDidMount() {
    if (this.args) {
      DocApps[this.functionName](this.ref.current, ...this.args)
    } else {
      DocApps[this.functionName](this.ref.current)
    }
  }

  render() {
    return <div className={styles.doodle}><div style={{position:"relative", height: this.height+"px"}} ref={this.ref}/></div>
  }
}
