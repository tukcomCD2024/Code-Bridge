import { defaultSettings } from "prosemirror-image-plugin";
import { v4 as uuidv4 } from "uuid";

export const imageSettings = {
  ...defaultSettings,
  hasTitle: false,
  minSize: 30,
  maxSize: 550,
  defaultAlt: localStorage.getItem("userId"),
  extraAttributes: {
    'owner': localStorage.getItem("userId"), 
    'data-guid': uuidv4(),
  },
};

export const imageNodeSpec = {
  inline: false,
  group: "block",
  attrs: {
    src: {},
    alt: { default: null },
    title: { default: null },
    guid: { default: null },
    author: { default: null },
  },
  parseDOM: [
    {
      tag: "img[src]",
      getAttrs: (dom) => ({
        src: dom.getAttribute("src"),
        alt: dom.getAttribute("alt"),
        title: dom.getAttribute("title"),
        guid: dom.getAttribute("data-guid"),
        author: dom.getAttribute("data-author"),
      }),
    },
  ],
  toDOM: node => ["img", { ...node.attrs, "data-guid": node.attrs.guid, "data-author": node.attrs.author }],
};
