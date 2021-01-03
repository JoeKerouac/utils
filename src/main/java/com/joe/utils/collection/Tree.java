package com.joe.utils.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joe.utils.common.string.StringUtils;

/**
 * 树结构，非线程安全
 * <p>
 * 用于目录存储，存储结构/a/b/c，表示包含a、a/b、a/b/c三个节点
 *
 * @author joe
 * @version 2018.04.11 16:12
 */
public class Tree<T> {
    private static final String ROOT = "root";
    private final Node<T> root;

    public Tree() {
        this.root = new Node<>(ROOT, null);
    }

    /**
     * 获取指定路径下的数据
     *
     * @param name
     *            路径名
     * @return 对应的数据
     */
    public T getData(String name) {
        Node<T> node = root.getNode(name);
        return node == null ? null : node.getData();
    }

    /**
     * 获取指定路径下的所有直系子节点的数据
     *
     * @param name
     *            路径名
     * @return 指定路径下的所有直系子节点的数据
     */
    public List<T> getAllChildData(String name) {
        Node<T> node = root.getNode(name);
        if (node == null) {
            return Collections.emptyList();
        } else {
            Map<String, Node<T>> nodeList = node.getChilds();
            if (nodeList.isEmpty()) {
                return Collections.emptyList();
            }
            List<T> datas = new ArrayList<>(nodeList.size());
            node.getChilds().values().forEach(n -> datas.add(n.getData()));
            return datas;
        }
    }

    /**
     * 更新节点数据
     *
     * @param name
     *            节点名
     * @param data
     *            要更新的数据
     * @return 节点原来的数据
     */
    public T updateData(String name, T data) {
        Node<T> node = root.getNode(name);
        T old = null;
        if (node != null) {
            old = node.setData(data);
        }
        return old;
    }

    /**
     * 判断指定路径是否存在
     *
     * @param name
     *            路径名
     * @return 返回true表示路径存在
     */
    public boolean exit(String name) {
        return root.getNode(name) != null;
    }

    /**
     * 添加节点
     *
     * @param name
     *            要添加的节点路径
     * @param data
     *            要添加的数据
     */
    public void add(String name, T data) {
        root.addChild(name, data);
    }

    /**
     * 删除节点，如果该节点下还有子节点则会抛出异常
     *
     * @param name
     *            节点名
     */
    public void delete(String name) {
        delete(name, false);
    }

    /**
     * 删除节点
     *
     * @param name
     *            节点名
     * @param recursion
     *            是否递归删除，true表示递归删除，如果传入false并且要删除的节点下有子节点则会抛出异常
     */
    public void delete(String name, boolean recursion) {
        root.delete(name, recursion);
    }

    /**
     * 清空所有节点
     */
    public void clear() {
        root.getChilds().clear();
    }

    public static class Node<T> {
        private static final String SYMBOL = "/";
        /**
         * 本节点名字
         */
        private final String name;
        /**
         * 本节点名字前缀，例如/abc/，如果是根节点那么该值为/
         */
        private final String prex;
        /**
         * 父节点
         */
        private final Node<T> parent;
        /**
         * 本节点数据
         */
        private T data;
        /**
         * 子节点
         */
        private final Map<String, Node<T>> childs;

        Node(String name, String prex, T data, Node<T> parent) {
            this.name = name;
            this.prex = prex;
            this.data = data;
            this.parent = parent;
            this.childs = new ConcurrentHashMap<>();
        }

        Node(String name, T data) {
            this(name, SYMBOL, data, null);
        }

        /**
         * 清空所有子节点
         */
        public void clear() {
            this.childs.clear();
        }

        /**
         * 将自己从树上删除（如果该节点有子节点也将一并删除）
         */
        public void deleteSelf() {
            if (isRoot()) {
                throw new IllegalStateException("root node can't be delete self");
            }
            parent.childs.remove(getName());
        }

        /**
         * 获取节点的完全限定名
         *
         * @return 节点的完全限定名（节点的完全路径，包含父级路径）
         */
        public String getFullName() {
            return prex + name;
        }

        /**
         * 获取本节点的名字
         *
         * @return 本节点名（不包含父级节点名）
         */
        public String getName() {
            return name;
        }

        /**
         * 获取节点数据
         *
         * @return 节点数据
         */
        public T getData() {
            return data;
        }

        /**
         * 更新当前节点数据
         *
         * @param data
         *            要更新的数据
         */
        public T setData(T data) {
            T old = this.data;
            this.data = data;
            return old;
        }

        /**
         * 当前节点是否是根节点
         *
         * @return 返回true表示当前节点是根节点
         */
        public boolean isRoot() {
            return parent == null;
        }

        /**
         * 添加子节点
         *
         * @param name
         *            节点名，如果名字是a/b/c这种将会递归添加a、b、c三个节点，并将数据设置到c节点
         * @param data
         *            节点数据
         * @return 添加上的节点
         */
        public Node<T> addChild(String name, T data) {
            if (name == null || name.isEmpty()) {
                throw new NullPointerException("node name must not be null or empty");
            }

            name = StringUtils.trim(name, SYMBOL);

            if (!name.contains("/")) {
                Node<T> node = new Node<>(name, getFullName() + SYMBOL, data, this);
                Node<T> old = addChildNode(name, node);
                if (old != null) {
                    old.setData(data);
                }
                return old;
            } else {
                String[] names = name.split("/");
                Node<T> oldNode = this;
                for (String str : names) {
                    Node<T> node = new Node<>(str, oldNode.getFullName() + SYMBOL, null, oldNode);
                    Node<T> old = oldNode.addChildNode(str, node);
                    oldNode = old == null ? node : old;
                }
                oldNode.setData(data);
                return oldNode;
            }
        }

        /**
         * 是否有子节点
         *
         * @return 返回true表示有子节点
         */
        public boolean hasChilds() {
            return !childs.isEmpty();
        }

        /**
         * 获取父节点
         *
         * @return 父节点
         */
        public Node<T> getParent() {
            return parent;
        }

        /**
         * 获取所有子节点
         *
         * @return 所有子节点
         */
        public Map<String, Node<T>> getChilds() {
            return childs;
        }

        /**
         * 获取指定子节点，可以递归
         *
         * @param name
         *            子节点名
         * @return 要获取的子节点，不存在时返回null
         */
        public Node<T> getNode(String name) {
            if (name == null || name.isEmpty()) {
                throw new NullPointerException("node name must not be null or empty");
            }

            name = StringUtils.trim(name, SYMBOL);

            if (!name.contains("/")) {
                return childs.get(name);
            }
            String[] names = name.split("/");
            Node<T> node = this;
            for (String str : names) {
                node = node.getNode(str);
                if (node == null) {
                    return null;
                }
            }
            return node;
        }

        /**
         * 删除该节点下的指定子节点
         *
         * @param name
         *            节点名
         * @param recursion
         *            是否递归删除，false表示不会递归删除而是会抛出异常
         */
        public void delete(String name, boolean recursion) {
            if (name == null || name.isEmpty()) {
                throw new NullPointerException("node name must not be null or empty");
            }

            name = StringUtils.trim(name, SYMBOL);

            Node<T> node = getNode(name);
            if (node == null) {
                return;
            }

            if (!recursion && node.hasChilds()) {
                throw new IllegalStateException("node[" + name + "] hava child node , can't be delete");
            }

            node.deleteSelf();
        }

        /**
         * 添加子节点
         *
         * @param name
         *            节点名，不包含/
         * @param node
         *            节点
         * @return 如果当前已经存在该名字的子节点那么返回当前节点，并且将其数据更新，如果当前不存在那么返回null
         */
        public Node<T> addChildNode(String name, Node<T> node) {
            return childs.putIfAbsent(name, node);
        }
    }
}
