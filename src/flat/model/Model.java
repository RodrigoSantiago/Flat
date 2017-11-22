package flat.model;

public class Model {
    float[] vertex; // x, y, z, normalx, normaly, normalz, uvx, uvy, (int)[material, bone1, bone2, bone3, bone4]
    int[] index;    // coord1, coord2, coord3

    Bone[] bones;
    ModelAnim[] anims;
    ModelMaterial[] materials;

    // TODO - shader atts { - }

    /*
    bones - { se tiver boones verifica se possui disponiveis, se nao, vai usar um array diferente pra fazer os transforms}
     */
}
