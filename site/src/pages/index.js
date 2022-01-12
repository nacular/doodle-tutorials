import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useBaseUrl from '@docusaurus/useBaseUrl';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/HomepageFeatures';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <img
                    className={clsx(siteConfig.logo, 'margin-vert--md')}
                    src={useBaseUrl('img/doodle.svg')}
                    alt={siteConfig.title}
                />
                <h1 className="hero__title">{siteConfig.tagline}</h1>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/docs/introduction">
                        Tutorials&nbsp;&nbsp;→
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={`${siteConfig.title}`}
            description="Tutorials for building apps using Doodle. Doodle is a pure Kotlin, UI framework for the Web, whose apps are also written entirely in Kotlin. These Applications do not use HTML, CSS styles or Javascript libraries. In fact, apps are not aware of the Browser (or Browser concepts) at all.">
            <HomepageHeader />
            <main>
                <HomepageFeatures />
            </main>
        </Layout>
    );
}