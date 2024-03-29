import { defaultSettings } from "prosemirror-image-plugin";

export const imageSettings = {
  ...defaultSettings,
  hasTitle: false,
  minSize: 30,
  maxSize: 550,
};

export const imageNodeSpec = {
  inline: false,
  group: "block",
  attrs: {
    src: {},
    alt: { default: null },
    title: { default: null },
    guid: { default: "" }, // Add guid attribute
  },
  parseDOM: [
    {
      tag: "img[src]",
      getAttrs: (dom) => ({
        src: dom.getAttribute("src"),
        alt: dom.getAttribute("alt"),
        title: dom.getAttribute("title"),
        guid: dom.getAttribute("data-guid"), // Handle guid attribute
      }),
    },
  ],
  toDOM: (node) => ["img", { ...node.attrs, "data-guid": node.attrs.guid }],
};
