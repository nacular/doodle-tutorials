import * as React from 'react';
import Box from '@mui/material/Box';
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';
import {useTreeViewApiRef} from "@mui/x-tree-view";
import {cloneElement, useEffect, useRef, useState} from 'react';

export function sanitize(items) {
    return items.map(c => sanitizeChildren(c, null))
}

function sanitizeChildren(item, parent: string) {
    const id = parent ? [parent, item.label].join('/') : item.label

    return {
        label   : item.label,
        id      : id,
        children: (item.children && item.children.map(c => sanitizeChildren(c, id))) ?? []
    }
}

export function BasicRichTreeView({children, items, defaultExpandedItems, defaultSelectedItem}) {
    const apiRef = useTreeViewApiRef()

    const handleNodeSelectionToggle = React.useCallback(
        (_, selectedItems: string[] | string,) => {
            setSelectedItem(selectedItems)
        },
        [apiRef]
    )

    const handleNodeExpansionToggle = React.useCallback(
        (_, items: string[])=> {
            setExpandedItems(items.concat(...defaultExpandedItems))
        },
        [apiRef]
    )

    const [expandedItems, setExpandedItems] = useState(defaultExpandedItems);
    const [selectedItem,   setSelectedItem] = useState(defaultSelectedItem );

    useEffect(() => {
        setSelectedItem(defaultSelectedItem)
    }, [apiRef]);

    return (
        <div style={{display: "flex"}}>
            <Box sx={{minWidth: 250}}>
                <RichTreeView
                    items                 = {items                    }
                    apiRef                = {apiRef                   }
                    // expandedItems         = {expandedItems            }
                    defaultExpandedItems  = {defaultExpandedItems     }
                    defaultSelectedItems  = {[defaultSelectedItem]    }
                    onSelectedItemsChange = {handleNodeSelectionToggle}
                    onExpandedItemsChange = {handleNodeExpansionToggle}
                />
            </Box>
            <div style={{marginLeft: "1em"}}>
                {children.map((item, i) =>
                    cloneElement(item, {
                        key   : i,
                        hidden: item.props.value !== selectedItem,
                    }),
                )}
            </div>
        </div>
    );
}