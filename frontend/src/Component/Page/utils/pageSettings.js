import { defaultSettings } from "prosemirror-image-plugin";

export const imageSettings = {
  ...defaultSettings,
  hasTitle: false,
  minSize: 30,
  maxSize: 550,
  extraAttributes: {
    'writer': localStorage.getItem("userId"),
    'data-guid': null,
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
    writer: { default: null },
  },
  parseDOM: [
    {
      tag: "img[src]",
      getAttrs: (dom) => ({
        src: dom.getAttribute("src"),
        alt: dom.getAttribute("alt"),
        title: dom.getAttribute("title"),
        guid: dom.getAttribute("data-guid"),
        writer: dom.getAttribute("data-writer"),
      }),
    },
  ],
  toDOM: node => ["img", { ...node.attrs, "data-guid": node.attrs.guid, "data-writer": node.attrs.author }, 0],
};
