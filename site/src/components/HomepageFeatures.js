import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
    {
        title: 'One Language',
        description: (
            <>
                Write your app once, entirely in <a href='https://kotlinlang.org'>Kotlin</a> and forget about the underlying platform.
            </>
        ),
    },
    {
        title: 'Many Platforms',
        description: (
            <>
                Deploy the same app to the Web (via JavaScript or WASM) or Desktop (via the JVM).
            </>
        ),
    },
    {
        title: 'Your Creativity',
        description: (
            <>
                Build beautiful, modern apps with pixel perfect UIs, fully customizable layouts and simple user input.
            </>
        ),
    },
];

function Feature({title, description}) {
    return (
        <div className={clsx('col col--4')}>
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
