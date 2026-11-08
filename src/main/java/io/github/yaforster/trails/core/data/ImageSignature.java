package io.github.yaforster.trails.core.data;

import java.util.List;

record ImageSignature(ImageType imageType, List<SignatureFragment> fragments) {

    static ImageSignature fromStart(ImageType imageType, byte[] bytes) {
        return withFragments(imageType, new SignatureFragment(0, bytes));
    }

    static ImageSignature withFragments(ImageType imageType, SignatureFragment... fragments) {
        return new ImageSignature(imageType, List.of(fragments));
    }

    boolean matches(byte[] content) {
        return fragments.stream()
                        .allMatch(fragment -> fragment.matches(content));
    }
}
