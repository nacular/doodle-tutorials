import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
    {
        title: 'Expressive',
        description: (
            <>
                Complex UIs are easy with powerful, vector-oriented rendering, fully customizable layouts and simple pointer and keyboard handling.
            </>
        ),
    },
    {
        title: 'Single Language',
        description: (
            <>
                Doodle apps are written entirely in <a href='https://kotlinlang.org'>Kotlin</a>. This means no CSS or Javascript libraries.
            </>
        ),
    },
    {
        title: 'Multi-platform',
        description: (
            <>
                Target both JS (Browser) and JVM (alpha) using common widgets and business logic.
            </>
        ),
    },
];
function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--4')}>
            <div className="text--center">
                {/*<Svg className={styles.featureSvg} alt={title} />*/}
            </div>
            <div className="text--center padding-horiz--md">
                <h1>{title}</h1>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
